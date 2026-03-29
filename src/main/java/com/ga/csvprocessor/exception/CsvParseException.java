package com.ga.csvprocessor.exception;

/**
 * Thrown when a CSV row cannot be parsed into an Employee record.
 */
public class CsvParseException extends RuntimeException {
    public CsvParseException(String message) {
        super(message);
    }

    public CsvParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
