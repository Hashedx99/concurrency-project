package com.ga.csvprocessor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


@Configuration
public class ThreadPoolConfig {

    private static final int MAX_CONCURRENT_WRITERS = 3;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService executorService() {
        int coreCount = Runtime.getRuntime().availableProcessors();
        System.out.println("[ThreadPoolConfig] Creating fixed thread pool with " + coreCount + " threads");
        return Executors.newFixedThreadPool(coreCount);
    }


    @Bean
    public Semaphore resultWriteSemaphore() {
        return new Semaphore(MAX_CONCURRENT_WRITERS, true); // fair = FIFO ordering
    }
}
