package com.ga.csvprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Concurrent CSV Data Processor application.
 * Starts the embedded Tomcat server and initializes the Spring context.
 */
@SpringBootApplication
public class CsvProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsvProcessorApplication.class, args);
    }
}
