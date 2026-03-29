package com.ga.csvprocessor.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ga.csvprocessor.entity.ProcessingResult;
import com.ga.csvprocessor.interfaces.FileHandler;
import com.ga.csvprocessor.util.CommonUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles persisting processing results as JSON documents.
 *
 */
@Component
public class ResultFileHandler implements FileHandler {

    private static final Path DATA_PATH = Paths.get("Data");
    private static final Path RESULTS_PATH = DATA_PATH.resolve("ProcessedResults");

    private final ObjectMapper objectMapper;

    public ResultFileHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Reads all ProcessingResult records from a previously saved batch file.
     *
     * @param source the batch file name (String) or File object
     * @return list of ProcessingResult, or empty list on failure
     */
    @Override
    public List<ProcessingResult> readFromFile(Object source) {
        File file = resolveFile(source);

        if (!file.exists()) {
            System.err.println("[ResultFileHandler] File not found: " + file.getAbsolutePath());
            return Collections.emptyList();
        }

        try {
            ProcessingResult[] results = objectMapper.readValue(file, ProcessingResult[].class);
            return Arrays.asList(results);
        } catch (Exception e) {
            System.err.println("[ResultFileHandler] Failed to read results: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Writes a list of ProcessingResult objects to a JSON document.
     *
     * @param name    the batch name / label (unused in file naming but kept for interface contract)
     * @param batchId the unique batch identifier — used as the file name
     * @param content the List<ProcessingResult> to persist
     * @return true if write succeeded
     */
    @Override
    public boolean writeToFile(String name, String batchId, Object content) {
        if (!(content instanceof List)) {
            System.err.println("[ResultFileHandler] Expected List<ProcessingResult>, got: "
                    + (content == null ? "null" : content.getClass().getSimpleName()));
            return false;
        }

        try {
            String fileName = batchId + ".json";
            String json = objectMapper.writeValueAsString(content);
            CommonUtil.createDirectoriesAndWriteFile(json, fileName, DATA_PATH, RESULTS_PATH);
            System.out.println("[ResultFileHandler] Results saved to Data/ProcessedResults/" + fileName);
            return true;
        } catch (Exception e) {
            System.err.println("[ResultFileHandler] Failed to write results: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lists all batch result files available on disk.
     *
     * @return array of batch JSON files, or empty array if none exist
     */
    public File[] listBatchFiles() {
        File dir = RESULTS_PATH.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            return new File[0];
        }
        File[] files = dir.listFiles(f -> f.getName().endsWith(".json"));
        return files == null ? new File[0] : files;
    }

    private File resolveFile(Object source) {
        if (source instanceof File) {
            return (File) source;
        }
        if (source instanceof String fileName) {
            return RESULTS_PATH.resolve(fileName.endsWith(".json") ? fileName : fileName + ".json").toFile();
        }
        throw new IllegalArgumentException("Expected File or String, got: "
                + (source == null ? "null" : source.getClass().getSimpleName()));
    }
}
