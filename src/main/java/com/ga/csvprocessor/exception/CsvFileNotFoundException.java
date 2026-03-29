package com.ga.csvprocessor.exception;

/**
 * Thrown when a CSV file cannot be found or read.
 */
public class CsvFileNotFoundException extends RuntimeException {
    public CsvFileNotFoundException(String message) {
        super(message);
    }

    public CsvFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
