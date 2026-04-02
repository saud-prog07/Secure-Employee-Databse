package com.example.employee.repository;

import com.example.employee.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing Payroll entities from the database.
 */
@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    /**
     * Find all payroll records for a specific employee.
     *
     * @param employeeId The ID of the employee
     * @param pageable   Pagination information
     * @return A page of payroll records
     */
    Page<Payroll> findByEmployeeId(Long employeeId, Pageable pageable);

    /**
     * Find a payroll record by employee ID and month.
     *
     * @param employeeId The ID of the employee
     * @param month      The month in YYYY-MM format
     * @return An optional containing the payroll record if found
     */
    Optional<Payroll> findByEmployeeIdAndMonth(Long employeeId, String month);

    /**
     * Find all payroll records for a specific month.
     *
     * @param month The month in YYYY-MM format
     * @return A list of all payroll records for that month
     */
    List<Payroll> findByMonth(String month);

    /**
     * Check if a payroll record already exists for an employee in a specific month.
     *
     * @param employeeId The ID of the employee
     * @param month      The month in YYYY-MM format
     * @return true if the record exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payroll p WHERE p.employeeId = :employeeId AND p.month = :month")
    boolean existsByEmployeeIdAndMonth(@Param("employeeId") Long employeeId, @Param("month") String month);

    /**
     * Get all payroll records ordered by month (descending).
     *
     * @param pageable Pagination information
     * @return A page of all payroll records
     */
    Page<Payroll> findAllByOrderByMonthDesc(Pageable pageable);
}
