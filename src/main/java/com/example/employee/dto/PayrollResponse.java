package com.example.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Payroll response data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Double baseSalary;
    private Double bonus;
    private Double deductions;
    private Double finalSalary;
    private String month;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
