package com.ga.csvprocessor.entity;

import com.ga.csvprocessor.enums.EmployeeRole;
import com.ga.csvprocessor.enums.ProcessingStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;

/**
 * Represents an employee record read from the CSV file.
 * Encapsulates all data required for salary increment calculation.
 */
@Getter
public class Employee {

    private final String name;
    private final double currentSalary;
    private final LocalDate joinedDate;
    private final EmployeeRole role;
    private final double projectCompletionPercentage;

    @Setter
    private double newSalary;
    @Setter
    private double totalRaisePercentage;
    @Setter
    private String skipReason;
    @Setter
    private ProcessingStatus status;

    public Employee(String name, double currentSalary, LocalDate joinedDate,
                    EmployeeRole role, double projectCompletionPercentage) {
        this.name = name;
        this.currentSalary = currentSalary;
        this.joinedDate = joinedDate;
        this.role = role;
        this.projectCompletionPercentage = projectCompletionPercentage;
        this.newSalary = currentSalary;
        this.totalRaisePercentage = 0.0;
        this.status = ProcessingStatus.PENDING;
    }


    public int getYearsOfService() {
        return Period.between(joinedDate, LocalDate.now()).getYears();
    }

    /**
     * Determines if the employee is eligible for any salary raise.
     * Employees who completed less than 60% of projects are ineligible.
     *
     * @return true if the employee qualifies for a raise
     */
    public boolean isEligibleForRaise() {
        return projectCompletionPercentage >= 0.6;
    }

    /**
     * Determines if the bonus multiplier (x1.5) should be applied.
     * Applies when the employee completed more than 80% of projects.
     *
     * @return true if the bonus multiplier should be applied
     */
    public boolean qualifiesForBonusMultiplier() {
        return projectCompletionPercentage > 0.8;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", role=" + role +
                ", salary=" + currentSalary +
                ", projects=" + projectCompletionPercentage + "%" +
                ", yearsOfService=" + getYearsOfService() +
                '}';
    }
}
