package com.ga.csvprocessor.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommonUtil {

    private CommonUtil() {
    }


    public static void printSeparatorLine() {
        System.out.println("-".repeat(100));
    }


    /**
     * Creates required directories (if absent) then writes a file to disk.
     *
     * @param fileContent    the string content to write
     * @param fileName       the name of the target file
     * @param dataPath       the top-level data directory path
     * @param nestedDataPath optional subdirectory; pass null to write directly to dataPath
     * @throws IOException if any I/O error occurs
     */
    public static void createDirectoriesAndWriteFile(String fileContent, String fileName,
                                                      Path dataPath, Path nestedDataPath) throws IOException {
        if (Files.notExists(dataPath)) {
            Files.createDirectories(dataPath);
        }

        if (nestedDataPath != null && Files.notExists(nestedDataPath)) {
            Files.createDirectories(nestedDataPath);
        }

        Path targetPath = nestedDataPath == null
                ? dataPath.resolve(fileName)
                : nestedDataPath.resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(targetPath)) {
            writer.write(fileContent);
            writer.flush();
        }
    }

    /**
     * Formats a salary value as a currency string.
     *
     * @param amount the raw double value
     * @return formatted string, e.g. "$45,000.00"
     */
    public static String formatSalary(double amount) {
        return String.format("$%,.2f", amount);
    }

    /**
     * Formats a percentage value for display.
     *
     * @param percentage the raw double (e.g. 0.05 or 5.0)
     * @param isDecimal  true if value is in decimal form (0.05), false if already in % (5.0)
     * @return formatted string, e.g. "5.00%"
     */
    public static String formatPercentage(double percentage, boolean isDecimal) {
        double display = isDecimal ? percentage * 100 : percentage;
        return String.format("%.2f%%", display);
    }
}
