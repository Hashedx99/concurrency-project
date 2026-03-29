package com.ga.csvprocessor.dto;

import com.ga.csvprocessor.entity.ProcessingResult;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ProcessingBatch {

    private final String batchId;
    private final LocalDateTime startedAt;
    @Setter
    private LocalDateTime completedAt;
    private final String sourceFileName;
    private final List<ProcessingResult> results;
    @Setter
    private long durationMillis;

    public ProcessingBatch(String batchId, String sourceFileName,
                            LocalDateTime startedAt, List<ProcessingResult> results) {
        this.batchId = batchId;
        this.sourceFileName = sourceFileName;
        this.startedAt = startedAt;
        this.results = results;
    }

    public long getTotalEmployees() {
        return results.size();
    }

    public long getProcessedCount() {
        return results.stream()
                .filter(r -> r.getStatus() != null)
                .filter(r -> switch (r.getStatus()) {
                    case COMPLETED, SKIPPED -> true;
                    default -> false;
                })
                .count();
    }

    public long getSkippedCount() {
        return results.stream()
                .filter(r -> r.getStatus() != null && r.getStatus().name().equals("SKIPPED"))
                .count();
    }

    public double getTotalSalaryBefore() {
        return results.stream().mapToDouble(ProcessingResult::getSalaryBefore).sum();
    }

    public double getTotalSalaryAfter() {
        return results.stream().mapToDouble(ProcessingResult::getSalaryAfter).sum();
    }

    public double getTotalSalaryIncrease() {
        return getTotalSalaryAfter() - getTotalSalaryBefore();
    }


}
