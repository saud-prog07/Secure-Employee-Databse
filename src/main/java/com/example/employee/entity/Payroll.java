package com.example.employee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity class representing a Payroll record in the database.
 * Stores payroll information for employees including salary, bonus, and deductions.
 */
@Entity
@Table(name = "payroll", indexes = {
    @Index(name = "idx_employee_id", columnList = "employee_id"),
    @Index(name = "idx_month", columnList = "month")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Employee ID is mandatory")
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @NotNull(message = "Base salary is mandatory")
    @Positive(message = "Base salary must be positive")
    @Column(nullable = false)
    private Double baseSalary;

    @Column(nullable = false)
    private Double bonus = 0.0;

    @Column(nullable = false)
    private Double deductions = 0.0;

    @Column(nullable = false)
    private Double finalSalary;

    @NotNull(message = "Month is mandatory")
    @Column(nullable = false)
    private String month; // Format: YYYY-MM (e.g., "2026-04")

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Calculates the final salary based on base salary, bonus, and deductions.
     * This method is called before saving to ensure consistency.
     */
    @PrePersist
    @PreUpdate
    public void calculateFinalSalary() {
        this.finalSalary = this.baseSalary + this.bonus - this.deductions;
    }
}
