package com.ga.csvprocessor.service;

import com.ga.csvprocessor.entity.Employee;
import com.ga.csvprocessor.entity.ProcessingResult;
import com.ga.csvprocessor.enums.ProcessingStatus;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Callable task that processes a single employee's salary increment.
 * Concurrency design:
 * - Each task runs on a thread from the fixed thread pool.
 * - The semaphore limits concurrent writes to the shared results list,
 *   preventing race conditions without blocking the entire pool.
 * - An AtomicInteger tracks how many employees have been processed globally,
 *   giving a thread-safe progress counter without synchronized blocks.
 */
public class EmployeeProcessorTask implements Callable<ProcessingResult> {

    private final Employee employee;
    private final SalaryCalculator calculator;
    private final Semaphore writeSemaphore;
    private final AtomicInteger processedCount;

    public EmployeeProcessorTask(Employee employee,
                                  SalaryCalculator calculator,
                                  Semaphore writeSemaphore,
                                  AtomicInteger processedCount) {
        this.employee = employee;
        this.calculator = calculator;
        this.writeSemaphore = writeSemaphore;
        this.processedCount = processedCount;
    }

    /**
     * Executes the salary calculation and returns a ProcessingResult.
     * The semaphore is acquired before building the result to prevent
     * concurrent writes from corrupting the result state.
     *
     * @return the ProcessingResult for this employee
     */
    @Override
    public ProcessingResult call() {
        try {
            // Run the pure calculation — this is thread-safe (stateless calculator)
            calculator.calculate(employee);

            // Acquire semaphore before reading computed state into the result object
            writeSemaphore.acquire();
            try {
                ProcessingResult result = new ProcessingResult(employee);
                processedCount.incrementAndGet();
                return result;
            } finally {
                writeSemaphore.release();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            employee.setStatus(ProcessingStatus.FAILED);
            employee.setSkipReason("Thread interrupted during processing");
            return new ProcessingResult(employee);
        } catch (Exception e) {
            employee.setStatus(ProcessingStatus.FAILED);
            employee.setSkipReason("Unexpected error: " + e.getMessage());
            System.out.println("[EmployeeProcessorTask] Failed to process "
                    + employee.getName() + ": " + e.getMessage());
            return new ProcessingResult(employee);
        }
    }
}
