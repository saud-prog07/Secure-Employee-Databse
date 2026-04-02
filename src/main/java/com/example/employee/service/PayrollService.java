package com.example.employee.service;

import com.example.employee.entity.Employee;
import com.example.employee.entity.Payroll;
import com.example.employee.dto.PayrollRequest;
import com.example.employee.dto.PayrollResponse;
import com.example.employee.exception.ResourceNotFoundException;
import com.example.employee.exception.BadRequestException;
import com.example.employee.repository.PayrollRepository;
import com.example.employee.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Service class for Payroll business logic.
 * Handles salary calculations, payroll generation, and retrieval.
 */
@Service
@Slf4j
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    // Regex pattern for YYYY-MM format validation
    private static final Pattern MONTH_PATTERN = Pattern.compile("\\d{4}-\\d{2}");

    public PayrollService(PayrollRepository payrollRepository, 
                         EmployeeRepository employeeRepository,
                         AuditLogService auditLogService) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
        this.auditLogService = auditLogService;
    }

    /**
     * Generates payroll for an employee.
     * Calculates finalSalary = baseSalary + bonus - deductions.
     *
     * @param payrollRequest The payroll request containing salary details
     * @return PayrollResponse with generated payroll information
     */
    @Transactional
    public PayrollResponse generatePayroll(PayrollRequest payrollRequest) {
        log.info("Generating payroll for employee ID: {} for month: {}", 
                payrollRequest.getEmployeeId(), payrollRequest.getMonth());

        // Validate month format
        validateMonthFormat(payrollRequest.getMonth());

        // Check if employee exists
        Employee employee = employeeRepository.findById(payrollRequest.getEmployeeId())
                .orElseThrow(() -> {
                    log.error("Employee not found with ID: {}", payrollRequest.getEmployeeId());
                    return new ResourceNotFoundException("Employee not found with ID: " + payrollRequest.getEmployeeId());
                });

        // Check if payroll already exists for this month
        if (payrollRepository.existsByEmployeeIdAndMonth(payrollRequest.getEmployeeId(), payrollRequest.getMonth())) {
            log.warn("Payroll already exists for employee ID: {} for month: {}", 
                    payrollRequest.getEmployeeId(), payrollRequest.getMonth());
            throw new BadRequestException("Payroll already exists for this employee in the given month");
        }

        // Create payroll entity
        Payroll payroll = new Payroll();
        payroll.setEmployeeId(payrollRequest.getEmployeeId());
        payroll.setBaseSalary(payrollRequest.getBaseSalary());
        payroll.setBonus(payrollRequest.getBonus() != null ? payrollRequest.getBonus() : 0.0);
        payroll.setDeductions(payrollRequest.getDeductions() != null ? payrollRequest.getDeductions() : 0.0);
        payroll.setMonth(payrollRequest.getMonth());

        // Save payroll (calculateFinalSalary will be called automatically)
        Payroll savedPayroll = payrollRepository.save(payroll);

        log.info("Payroll generated successfully for employee ID: {}. Final salary: {}", 
                savedPayroll.getEmployeeId(), savedPayroll.getFinalSalary());

        // Log audit trail
        auditLogService.logAction("PAYROLL_GENERATED", 
                "Payroll generated for employee: " + employee.getName() + " for month: " + payrollRequest.getMonth());

        return convertToResponse(savedPayroll, employee.getName());
    }

    /**
     * Retrieves all payroll records for a specific employee.
     *
     * @param employeeId The ID of the employee
     * @param pageable   Pagination information
     * @return A page of payroll records
     */
    @Transactional(readOnly = true)
    public Page<PayrollResponse> getPayrollByEmployeeId(Long employeeId, Pageable pageable) {
        log.info("Fetching payroll records for employee ID: {}", employeeId);

        // Check if employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.error("Employee not found with ID: {}", employeeId);
                    return new ResourceNotFoundException("Employee not found with ID: " + employeeId);
                });

        Page<Payroll> payrolls = payrollRepository.findByEmployeeId(employeeId, pageable);
        return payrolls.map(payroll -> convertToResponse(payroll, employee.getName()));
    }

    /**
     * Retrieves a single payroll record by ID.
     *
     * @param payrollId The ID of the payroll record
     * @return PayrollResponse with payroll information
     */
    @Transactional(readOnly = true)
    public PayrollResponse getPayrollById(Long payrollId) {
        log.info("Fetching payroll record with ID: {}", payrollId);

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> {
                    log.error("Payroll not found with ID: {}", payrollId);
                    return new ResourceNotFoundException("Payroll not found with ID: " + payrollId);
                });

        Employee employee = employeeRepository.findById(payroll.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return convertToResponse(payroll, employee.getName());
    }

    /**
     * Retrieves all payroll records for a specific month.
     *
     * @param month The month in YYYY-MM format
     * @return A list of payroll records
     */
    @Transactional(readOnly = true)
    public List<Payroll> getPayrollByMonth(String month) {
        log.info("Fetching payroll records for month: {}", month);
        validateMonthFormat(month);
        return payrollRepository.findByMonth(month);
    }

    /**
     * Retrieves all payroll records with pagination.
     *
     * @param pageable Pagination information
     * @return A page of all payroll records
     */
    @Transactional(readOnly = true)
    public Page<PayrollResponse> getAllPayroll(Pageable pageable) {
        log.info("Fetching all payroll records with pagination");

        Page<Payroll> payrolls = payrollRepository.findAllByOrderByMonthDesc(pageable);
        return payrolls.map(payroll -> {
            Employee employee = employeeRepository.findById(payroll.getEmployeeId())
                    .orElse(null);
            String employeeName = employee != null ? employee.getName() : "Unknown";
            return convertToResponse(payroll, employeeName);
        });
    }

    /**
     * Updates a payroll record.
     *
     * @param payrollId      The ID of the payroll to update
     * @param payrollRequest The updated payroll details
     * @return Updated PayrollResponse
     */
    @Transactional
    public PayrollResponse updatePayroll(Long payrollId, PayrollRequest payrollRequest) {
        log.info("Updating payroll record with ID: {}", payrollId);

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found with ID: " + payrollId));

        Employee employee = employeeRepository.findById(payroll.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Update fields
        payroll.setBaseSalary(payrollRequest.getBaseSalary());
        payroll.setBonus(payrollRequest.getBonus() != null ? payrollRequest.getBonus() : 0.0);
        payroll.setDeductions(payrollRequest.getDeductions() != null ? payrollRequest.getDeductions() : 0.0);

        Payroll updatedPayroll = payrollRepository.save(payroll);
        log.info("Payroll updated successfully. New final salary: {}", updatedPayroll.getFinalSalary());

        auditLogService.logAction("PAYROLL_UPDATED", 
                "Payroll updated for employee: " + employee.getName());

        return convertToResponse(updatedPayroll, employee.getName());
    }

    /**
     * Deletes a payroll record.
     *
     * @param payrollId The ID of the payroll to delete
     */
    @Transactional
    public void deletePayroll(Long payrollId) {
        log.info("Deleting payroll record with ID: {}", payrollId);

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found with ID: " + payrollId));

        Employee employee = employeeRepository.findById(payroll.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        payrollRepository.deleteById(payrollId);
        log.info("Payroll deleted successfully");

        auditLogService.logAction("PAYROLL_DELETED", 
                "Payroll deleted for employee: " + employee.getName() + " for month: " + payroll.getMonth());
    }

    /**
     * Validates the month format (YYYY-MM).
     *
     * @param month The month string to validate
     */
    private void validateMonthFormat(String month) {
        if (!MONTH_PATTERN.matcher(month).matches()) {
            throw new BadRequestException("Invalid month format. Expected format: YYYY-MM");
        }
    }

    /**
     * Converts a Payroll entity to a PayrollResponse DTO.
     *
     * @param payroll      The payroll entity
     * @param employeeName The name of the employee
     * @return PayrollResponse DTO
     */
    private PayrollResponse convertToResponse(Payroll payroll, String employeeName) {
        PayrollResponse response = new PayrollResponse();
        response.setId(payroll.getId());
        response.setEmployeeId(payroll.getEmployeeId());
        response.setEmployeeName(employeeName);
        response.setBaseSalary(payroll.getBaseSalary());
        response.setBonus(payroll.getBonus());
        response.setDeductions(payroll.getDeductions());
        response.setFinalSalary(payroll.getFinalSalary());
        response.setMonth(payroll.getMonth());
        response.setCreatedAt(payroll.getCreatedAt());
        response.setUpdatedAt(payroll.getUpdatedAt());
        return response;
    }
}
