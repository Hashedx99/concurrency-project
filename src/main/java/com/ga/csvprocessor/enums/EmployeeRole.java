package com.ga.csvprocessor.enums;

import lombok.Getter;


@Getter
public enum EmployeeRole {

    DIRECTOR(0.05),
    MANAGER(0.02),
    EMPLOYEE(0.01);

    private final double baseRaisePercentage;

    EmployeeRole(double baseRaisePercentage) {
        this.baseRaisePercentage = baseRaisePercentage;
    }

    /**
     * Parses a role string from CSV (case-insensitive).
     *
     * @param value the raw string from CSV
     * @return the matching EmployeeRole
     * @throws IllegalArgumentException if no match is found
     */
    public static EmployeeRole fromString(String value) {
        for (EmployeeRole role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown employee role: " + value);
    }
}
