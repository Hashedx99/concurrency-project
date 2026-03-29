package com.ga.csvprocessor.controller;

import com.ga.csvprocessor.dto.ProcessingBatch;
import com.ga.csvprocessor.entity.ProcessingResult;
import com.ga.csvprocessor.repository.CsvFileHandler;
import com.ga.csvprocessor.service.CsvProcessorService;
import com.ga.csvprocessor.service.SalaryCalculator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CsvProcessorController {

    private static final Path UPLOAD_DIR = Paths.get("Data", "Uploads").toAbsolutePath();

    private final CsvProcessorService processorService;
    private final CsvFileHandler csvFileHandler;
    private final SalaryCalculator salaryCalculator;

    public CsvProcessorController(CsvProcessorService processorService, CsvFileHandler csvFileHandler,
                                  SalaryCalculator salaryCalculator) {
        this.processorService = processorService;
        this.csvFileHandler = csvFileHandler;
        this.salaryCalculator = salaryCalculator;
    }

    @PostMapping("/process")
    public ResponseEntity<?> processFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "File is empty");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            Files.createDirectories(UPLOAD_DIR);
            Path savedPath = UPLOAD_DIR.resolve(file.getOriginalFilename());
            file.transferTo(savedPath.toFile());

            ProcessingBatch batch = processorService.processCsvFile(savedPath.toString());
            return ResponseEntity.ok(batch);

        } catch (Exception e) {
            System.out.println("[CsvProcessorController] Error processing file: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process file");
            error.put("details", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<?> viewBatch(@PathVariable String batchId) {
        try {
            List<ProcessingResult> results = processorService.loadBatchResults(batchId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.out.println("[CsvProcessorController] Batch not found: " + batchId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/uploads")
    public ResponseEntity<?> listUploads() {
        try {
            File dir = UPLOAD_DIR.toFile();
            List<String> files = new ArrayList<>();

            if (dir.exists()) {
                File[] csvFiles = dir.listFiles(f -> f.getName().endsWith(".csv"));
                if (csvFiles != null) {
                    for (File f : csvFiles) {
                        files.add(f.getAbsolutePath());
                    }
                }
            }

            return ResponseEntity.ok(files);
        } catch (Exception e) {
            System.out.println("[CsvProcessorController] Error listing uploads: " + e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to list uploads");
            error.put("details", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/batches")
    public ResponseEntity<?> listBatches() {
        try {
            List<String> batches = processorService.listBatchFiles();
            return ResponseEntity.ok(batches);
        } catch (Exception e) {
            System.out.println("[CsvProcessorController] Error listing batches: " + e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to list batches");
            error.put("details", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
