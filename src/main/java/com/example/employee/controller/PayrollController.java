package com.example.employee.controller;

import com.example.employee.dto.ApiResponse;
import com.example.employee.dto.PayrollRequest;
import com.example.employee.dto.PayrollResponse;
import com.example.employee.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Payroll entities.
 * Provides endpoints for CRUD operations and payroll generation.
 * Access is restricted based on user roles: ADMIN can generate, HR can view.
 */
@RestController
@RequestMapping("/api/payroll")
@Slf4j
@Tag(name = "Payroll", description = "The Payroll Management API")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    /**
     * Generates payroll for an employee.
     * Only accessible to ADMIN users.
     *
     * @param payrollRequest The payroll details
     * @return ApiResponse with the generated payroll
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate Payroll", 
               description = "Generate payroll for an employee. Only ADMIN can access.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payroll generated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payroll data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<PayrollResponse>> generatePayroll(@Valid @RequestBody PayrollRequest payrollRequest) {
        log.info("POST /api/payroll/generate - Generating payroll");
        PayrollResponse payroll = payrollService.generatePayroll(payrollRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payroll generated successfully", payroll));
    }

    /**
     * Retrieves all payroll records for a specific employee.
     * Accessible to ADMIN and HR users.
     *
     * @param employeeId The ID of the employee
     * @param page       The page number (0-indexed)
     * @param size       The page size
     * @param sort       The sort criteria
     * @return ApiResponse with paginated payroll records
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @Operation(summary = "Get Payroll by Employee ID", 
               description = "Retrieve payroll records for a specific employee. ADMIN and HR can access.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payroll records retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<Page<PayrollResponse>>> getPayrollByEmployeeId(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "month") String sort) {
        log.info("GET /api/payroll/employee/{} - Fetching payroll records", employeeId);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, sort);
        Page<PayrollResponse> payrolls = payrollService.getPayrollByEmployeeId(employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully", payrolls));
    }

    /**
     * Retrieves a single payroll record by ID.
     * Accessible to ADMIN and HR users.
     *
     * @param payrollId The ID of the payroll record
     * @return ApiResponse with the payroll details
     */
    @GetMapping("/{payrollId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @Operation(summary = "Get Payroll by ID", 
               description = "Retrieve a specific payroll record. ADMIN and HR can access.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payroll retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    public ResponseEntity<ApiResponse<PayrollResponse>> getPayrollById(@PathVariable Long payrollId) {
        log.info("GET /api/payroll/{} - Fetching payroll record", payrollId);
        PayrollResponse payroll = payrollService.getPayrollById(payrollId);
        return ResponseEntity.ok(ApiResponse.success("Payroll retrieved successfully", payroll));
    }

    /**
     * Retrieves all payroll records with pagination.
     * Accessible to ADMIN and HR users.
     *
     * @param page The page number (0-indexed)
     * @param size The page size
     * @param sort The sort criteria
     * @return ApiResponse with paginated payroll records
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @Operation(summary = "Get All Payroll Records", 
               description = "Retrieve all payroll records with pagination. ADMIN and HR can access.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payroll records retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Page<PayrollResponse>>> getAllPayroll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "month") String sort) {
        log.info("GET /api/payroll - Fetching all payroll records");
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, sort);
        Page<PayrollResponse> payrolls = payrollService.getAllPayroll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully", payrolls));
    }

    /**
     * Updates a payroll record.
     * Only accessible to ADMIN users.
     *
     * @param payrollId      The ID of the payroll to update
     * @param payrollRequest The updated payroll details
     * @return ApiResponse with the updated payroll
     */
    @PutMapping("/{payrollId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Payroll", 
               description = "Update payroll information. Only ADMIN can access.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payroll updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payroll data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    public ResponseEntity<ApiResponse<PayrollResponse>> updatePayroll(
            @PathVariable Long payrollId,
            @Valid @RequestBody PayrollRequest payrollRequest) {
        log.info("PUT /api/payroll/{} - Updating payroll record", payrollId);
        PayrollResponse updatedPayroll = payrollService.updatePayroll(payrollId, payrollRequest);
        return ResponseEntity.ok(ApiResponse.success("Payroll updated successfully", updatedPayroll));
    }

    /**
     * Deletes a payroll record.
     * Only accessible to ADMIN users.
     *
     * @param payrollId The ID of the payroll to delete
     * @return ApiResponse with success status
     */
    @DeleteMapping("/{payrollId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Payroll", 
               description = "Delete a payroll record. Only ADMIN can access.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payroll deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    public ResponseEntity<ApiResponse<Void>> deletePayroll(@PathVariable Long payrollId) {
        log.info("DELETE /api/payroll/{} - Deleting payroll record", payrollId);
        payrollService.deletePayroll(payrollId);
        return ResponseEntity.ok(ApiResponse.success("Payroll deleted successfully", null));
    }
}
