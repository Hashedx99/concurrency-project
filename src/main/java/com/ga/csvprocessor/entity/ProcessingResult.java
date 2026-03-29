package com.ga.csvprocessor.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ga.csvprocessor.enums.EmployeeRole;
import com.ga.csvprocessor.enums.ProcessingStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable result record produced after processing an employee's salary.
 * Stored as a JSON document — one file per processed batch.
 */
@Getter
public class ProcessingResult {

    @JsonProperty("employeeName")
    private final String employeeName;

    @JsonProperty("role")
    private final EmployeeRole role;

    @JsonProperty("joinedDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate joinedDate;

    @JsonProperty("yearsOfService")
    private final int yearsOfService;

    @JsonProperty("projectCompletionPercentage")
    private final double projectCompletionPercentage;

    @JsonProperty("salaryBefore")
    private final double salaryBefore;

    @JsonProperty("salaryAfter")
    private final double salaryAfter;

    @JsonProperty("salaryIncrease")
    private final double salaryIncrease;

    @JsonProperty("totalRaisePercentage")
    private final double totalRaisePercentage;

    @JsonProperty("bonusMultiplierApplied")
    private final boolean bonusMultiplierApplied;

    @JsonProperty("status")
    private final ProcessingStatus status;

    @JsonProperty("skipReason")
    private final String skipReason;

    @JsonProperty("processedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime processedAt;

    @JsonProperty("threadName")
    private final String threadName;

    public ProcessingResult(Employee employee) {
        this.employeeName = employee.getName();
        this.role = employee.getRole();
        this.joinedDate = employee.getJoinedDate();
        this.yearsOfService = employee.getYearsOfService();
        this.projectCompletionPercentage = employee.getProjectCompletionPercentage();
        this.salaryBefore = employee.getCurrentSalary();
        this.salaryAfter = employee.getNewSalary();
        this.salaryIncrease = employee.getNewSalary() - employee.getCurrentSalary();
        this.totalRaisePercentage = employee.getTotalRaisePercentage();
        this.bonusMultiplierApplied = employee.qualifiesForBonusMultiplier() && employee.isEligibleForRaise();
        this.status = employee.getStatus();
        this.skipReason = employee.getSkipReason();
        this.processedAt = LocalDateTime.now();
        this.threadName = Thread.currentThread().getName();
    }

    public ProcessingResult() {
        this.employeeName = null;
        this.role = null;
        this.joinedDate = null;
        this.yearsOfService = 0;
        this.projectCompletionPercentage = 0;
        this.salaryBefore = 0;
        this.salaryAfter = 0;
        this.salaryIncrease = 0;
        this.totalRaisePercentage = 0;
        this.bonusMultiplierApplied = false;
        this.status = null;
        this.skipReason = null;
        this.processedAt = null;
        this.threadName = null;
    }

}
