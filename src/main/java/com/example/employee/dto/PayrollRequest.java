package com.example.employee.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Payroll creation/update requests.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollRequest {

    @NotNull(message = "Employee ID is mandatory")
    private Long employeeId;

    @NotNull(message = "Base salary is mandatory")
    @Positive(message = "Base salary must be positive")
    private Double baseSalary;

    private Double bonus = 0.0;

    private Double deductions = 0.0;

    @NotNull(message = "Month is mandatory")
    private String month; // Format: YYYY-MM
}
