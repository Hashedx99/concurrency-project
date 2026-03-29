package com.ga.csvprocessor.service;

import com.ga.csvprocessor.dto.ProcessingBatch;
import com.ga.csvprocessor.entity.Employee;
import com.ga.csvprocessor.entity.ProcessingResult;
import com.ga.csvprocessor.repository.CsvFileHandler;
import com.ga.csvprocessor.repository.ResultFileHandler;
import com.ga.csvprocessor.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CsvProcessorService {

    private static final DateTimeFormatter BATCH_ID_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final CsvFileHandler csvFileHandler;
    private final ResultFileHandler resultFileHandler;
    private final SalaryCalculator salaryCalculator;
    private final ExecutorService executorService;
    private final Semaphore resultWriteSemaphore;

    public CsvProcessorService(CsvFileHandler csvFileHandler,
                                ResultFileHandler resultFileHandler,
                                SalaryCalculator salaryCalculator,
                                ExecutorService executorService,
                                Semaphore resultWriteSemaphore) {
        this.csvFileHandler = csvFileHandler;
        this.resultFileHandler = resultFileHandler;
        this.salaryCalculator = salaryCalculator;
        this.executorService = executorService;
        this.resultWriteSemaphore = resultWriteSemaphore;
    }

    /**
     * Processes the entire employee CSV file concurrently.
     * Steps:
     * 1. Read all employees from CSV
     * 2. Submit one Callable task per employee to the thread pool
     * 3. Collect all Future results (blocking until all complete)
     * 4. Persist the batch results as a JSON document
     * 5. Return the batch summary
     *
     * @param csvFilePath path to the source CSV file
     * @return a {@link ProcessingBatch} containing all results and summary stats
     */
    public ProcessingBatch processCsvFile(String csvFilePath) {
        LocalDateTime startedAt = LocalDateTime.now();
        String batchId = generateBatchId(startedAt);

        CommonUtil.printSeparatorLine();
        System.out.println("[CsvProcessorService] Starting batch: " + batchId);
        System.out.println("[CsvProcessorService] Source file:   " + csvFilePath);
        CommonUtil.printSeparatorLine();

        // Step 1: read employees
        List<Employee> employees = csvFileHandler.readFromFile(new File(csvFilePath));
        if (employees.isEmpty()) {
            System.out.println("[CsvProcessorService] No employees found — nothing to process.");
            return buildEmptyBatch(batchId, csvFilePath, startedAt);
        }

        System.out.println("[CsvProcessorService] Employees loaded: " + employees.size());

        // Step 2: submit tasks
        AtomicInteger processedCount = new AtomicInteger(0);
        List<Future<ProcessingResult>> futures = new ArrayList<>();

        for (Employee employee : employees) {
            EmployeeProcessorTask task = new EmployeeProcessorTask(
                    employee, salaryCalculator, resultWriteSemaphore, processedCount);
            futures.add(executorService.submit(task));
        }

        // Step 3: collect results
        List<ProcessingResult> results = collectResults(futures, employees.size());

        long durationMillis = System.currentTimeMillis()
                - startedAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        // Step 4: persist
        resultFileHandler.writeToFile("batch", batchId, results);

        // Step 5: assemble batch summary
        ProcessingBatch batch = new ProcessingBatch(batchId, extractFileName(csvFilePath), startedAt, results);
        batch.setCompletedAt(LocalDateTime.now());
        batch.setDurationMillis(durationMillis);

        printSummary(batch);
        return batch;
    }

    /**
     * Returns the list of all previously saved batch result files.
     *
     * @return list of file names found in Data/ProcessedResults/
     */
    public List<String> listBatchFiles() {
        File[] files = resultFileHandler.listBatchFiles();
        List<String> names = new ArrayList<>();
        for (File f : files) {
            names.add(f.getName().replace(".json", ""));
        }
        return names;
    }

    /**
     * Loads the results of a previously processed batch from disk.
     *
     * @param batchId the batch identifier (without .json extension)
     * @return list of ProcessingResult records for that batch
     */
    public List<ProcessingResult> loadBatchResults(String batchId) {
        return resultFileHandler.readFromFile(batchId);
    }

    // --- Private helpers ---

    /**
     * Collects Future results one by one, handling interruptions and task failures gracefully.
     */
    private List<ProcessingResult> collectResults(List<Future<ProcessingResult>> futures, int total) {
        List<ProcessingResult> results = new ArrayList<>(total);

        for (Future<ProcessingResult> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[CsvProcessorService] Interrupted while waiting for task result.");
            } catch (ExecutionException e) {
                System.out.println("[CsvProcessorService] Task execution failed: " + e.getCause().getMessage());
            }
        }

        return results;
    }

    private String generateBatchId(LocalDateTime at) {
        return at.format(BATCH_ID_FORMATTER) + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String extractFileName(String path) {
        return new File(path).getName();
    }

    private ProcessingBatch buildEmptyBatch(String batchId, String csvFilePath, LocalDateTime startedAt) {
        ProcessingBatch batch = new ProcessingBatch(
                batchId, extractFileName(csvFilePath), startedAt, new ArrayList<>());
        batch.setCompletedAt(LocalDateTime.now());
        batch.setDurationMillis(0);
        return batch;
    }

    private void printSummary(ProcessingBatch batch) {
        CommonUtil.printSeparatorLine();
        System.out.println("[CsvProcessorService] Batch complete: " + batch.getBatchId());
        System.out.printf("  Total employees : %d%n", batch.getTotalEmployees());
        System.out.printf("  Processed       : %d%n", batch.getProcessedCount());
        System.out.printf("  Skipped         : %d%n", batch.getSkippedCount());
        System.out.printf("  Salary before   : %s%n", CommonUtil.formatSalary(batch.getTotalSalaryBefore()));
        System.out.printf("  Salary after    : %s%n", CommonUtil.formatSalary(batch.getTotalSalaryAfter()));
        System.out.printf("  Total increase  : %s%n", CommonUtil.formatSalary(batch.getTotalSalaryIncrease()));
        System.out.printf("  Duration        : %d ms%n", batch.getDurationMillis());
        CommonUtil.printSeparatorLine();
    }
}
