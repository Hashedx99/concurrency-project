package com.ga.csvprocessor.service;

import com.ga.csvprocessor.entity.Employee;
import com.ga.csvprocessor.enums.EmployeeRole;
import com.ga.csvprocessor.enums.ProcessingStatus;
import com.ga.csvprocessor.util.CommonUtil;
import org.springframework.stereotype.Component;

/**
 * Business rules applied in order:
 * <ol>
 *   <li>If project completion &lt; 60% → no raise (SKIPPED)</li>
 *   <li>Years of completed service × role raise % (only if ≥ 1 year served)</li>
 *   <li>Role raise % (Director 5%, Manager 2%, Employee 1%)</li>
 *   <li>If project completion &gt; 80% → multiply total raise by 1.5 (bonus)</li>
 * </ol>
 *
 * This class is stateless so it is safely shared across all worker threads.
 */
@Component
public class SalaryCalculator {

    private static final double YEARS_RAISE_PER_YEAR = 0.02;
    private static final double BONUS_MULTIPLIER = 1.5;

    /**
     * Applies the salary increment rules to the given employee in-place.
     * This method is intentionally pure: it reads employee data and mutates
     * only the computed fields (newSalary, totalRaisePercentage, status).
     *
     * @param employee the employee record to process (mutated in-place)
     */
    public void calculate(Employee employee) {
        System.out.printf("[%s] Processing: %s%n",
                Thread.currentThread().getName(), employee.getName());

        // Rule 1: below 60% completion → no raise
        if (!employee.isEligibleForRaise()) {
            employee.setNewSalary(employee.getCurrentSalary());
            employee.setTotalRaisePercentage(0.0);
            employee.setStatus(ProcessingStatus.SKIPPED);
            employee.setSkipReason("Project completion " +  CommonUtil.formatPercentage(employee.getProjectCompletionPercentage(), true)
                    + "% is below the 60% threshold");
            System.out.printf("[%s] Skipped %s — below 60%% project threshold%n",
                    Thread.currentThread().getName(), employee.getName());
            return;
        }

        employee.setStatus(ProcessingStatus.PROCESSING);

        double raisePercentage = 0.0;

        // Rule 2: 2% per completed year of service
        int years = employee.getYearsOfService();
        if (years >= 1) {
            raisePercentage += years * YEARS_RAISE_PER_YEAR;
        }

        // Rule 3: role-based raise
        raisePercentage += employee.getRole().getBaseRaisePercentage();

        // Rule 4: bonus multiplier for >80% project completion
        if (employee.qualifiesForBonusMultiplier()) {
            raisePercentage *= BONUS_MULTIPLIER;
        }

        double salaryIncrease = employee.getCurrentSalary() * raisePercentage;
        double newSalary = employee.getCurrentSalary() + salaryIncrease;

        employee.setNewSalary(newSalary);
        employee.setTotalRaisePercentage(raisePercentage);
        employee.setStatus(ProcessingStatus.COMPLETED);

        System.out.printf("[%s] Completed %s | %s → %s (+%s)%n",
                Thread.currentThread().getName(),
                employee.getName(),
                CommonUtil.formatSalary(employee.getCurrentSalary()),
                CommonUtil.formatSalary(newSalary),
                CommonUtil.formatPercentage(raisePercentage, true));
    }
}
