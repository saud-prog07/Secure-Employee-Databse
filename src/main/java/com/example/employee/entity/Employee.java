package com.example.employee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity class representing an Employee record in the database.
 */
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 10)
    private String employeeId;

    @NotBlank(message = "Name is mandatory")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    private String department;

    @Positive(message = "Salary must be positive")
    private Double salary;

    /**
     * Soft delete flag. If true, the record is logically deleted but remains in the DB.
     */
    @Column(nullable = false)
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.PENDING;

    /**
     * Username of the admin who approved this employee.
     */
    @Column(name = "approved_by")
    private String approvedBy;

    /**
     * Timestamp when the employee was approved.
     */
    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    /**
     * Two-factor authentication enabled flag.
     */
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;

    /**
     * Two-factor authentication secret key for TOTP.
     */
    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
}
