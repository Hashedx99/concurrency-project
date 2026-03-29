package com.ga.csvprocessor.repository;

import com.ga.csvprocessor.entity.Employee;
import com.ga.csvprocessor.enums.EmployeeRole;
import com.ga.csvprocessor.exception.CsvFileNotFoundException;
import com.ga.csvprocessor.exception.CsvParseException;
import com.ga.csvprocessor.interfaces.FileHandler;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading employee data from CSV files.
 * Expected CSV columns (no header):
 *   id, name, salary, joined_date, role, project_completion_percentage
 *
 */
@Component
public class CsvFileHandler implements FileHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int EXPECTED_COLUMN_COUNT = 6;

    /**
     * Reads and parses all employee records from a CSV file.
     *
     * @param source a File or String path to the CSV file
     * @return a List of Employee objects, or empty list on failure
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Employee> readFromFile(Object source) {
        File file = resolveFile(source);

        if (!file.exists() || !file.isFile()) {
            throw new CsvFileNotFoundException("CSV file not found: " + file.getAbsolutePath());
        }

        List<Employee> employees = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(file))) {

            String[] row;
            int lineNumber = 1;

            while ((row = reader.readNext()) != null) {
                lineNumber++;
                try {
                    Employee employee = parseRow(row, lineNumber);
                    employees.add(employee);
                } catch (CsvParseException e) {
                    System.err.println("[CsvFileHandler] Skipping row " + lineNumber + ": " + e.getMessage());
                }
            }

        } catch (IOException | CsvValidationException e) {
            throw new CsvFileNotFoundException("Failed to read CSV file: " + file.getName(), e);
        }

        System.out.println("[CsvFileHandler] Loaded " + employees.size() + " employee record(s) from " + file.getName());
        return employees;
    }

    /**
     * Not used for CSV reading — writes are handled by ResultFileHandler.
     */
    @Override
    public boolean writeToFile(String name, String id, Object content) {
        throw new UnsupportedOperationException("CsvFileHandler is read-only. Use ResultFileHandler for writes.");
    }

    private File resolveFile(Object source) {
        if (source instanceof File) {
            return (File) source;
        }
        if (source instanceof String) {
            return new File((String) source);
        }
        throw new IllegalArgumentException("CsvFileHandler expects a File or String path, got: "
                + (source == null ? "null" : source.getClass().getSimpleName()));
    }

    /**
     * Parses a single CSV row into an Employee entity.
     *
     * @param row        the raw string array from opencsv
     * @param lineNumber the line number in the file (for error messages)
     * @return a populated Employee object
     * @throws CsvParseException if the row is malformed
     */
    private Employee parseRow(String[] row, int lineNumber) {
        if (row.length < EXPECTED_COLUMN_COUNT) {
            throw new CsvParseException("Row " + lineNumber + " has " + row.length
                    + " columns, expected " + (EXPECTED_COLUMN_COUNT ));
        }

        try {
            String name = row[1].trim();
            double salary = Double.parseDouble(row[2].trim());
            LocalDate joinedDate = LocalDate.parse(row[3].trim(), DATE_FORMATTER);
            EmployeeRole role = EmployeeRole.fromString(row[4].trim());
            double projectCompletion = Double.parseDouble(row[5].trim());

            if (name.isBlank()) {
                throw new CsvParseException("Employee name is blank at row " + lineNumber);
            }
            if (salary < 0) {
                throw new CsvParseException("Negative salary at row " + lineNumber + " for: " + name);
            }
            if (projectCompletion < 0 || projectCompletion > 1) {
                throw new CsvParseException("Project completion " + projectCompletion
                        + " out of range [0–100] at row " + lineNumber);
            }

            return new Employee(name, salary, joinedDate, role, projectCompletion);

        } catch (NumberFormatException e) {
            throw new CsvParseException("Invalid number at row " + lineNumber + ": " + e.getMessage(), e);
        } catch (DateTimeParseException e) {
            throw new CsvParseException("Invalid date format at row " + lineNumber
                    + " (expected yyyy-MM-dd): " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new CsvParseException("Parse error at row " + lineNumber + ": " + e.getMessage(), e);
        }
    }

}
