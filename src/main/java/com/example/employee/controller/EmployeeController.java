package com.example.employee.controller;

import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import com.example.employee.service.EmployeeService;
import com.example.employee.dto.ApiResponse;
import com.example.employee.dto.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for managing Employee entities.
 * Provides endpoints for CRUD operations, search, and pagination.
 */
@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee", description = "The Employee Management API")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Retrieves a paginated list of all APPROVED employees.
     *
     * @param page The page number (0-indexed).
     * @param size The number of items per page.
     * @param sort An array specifying the sort field and direction (e.g., "id,asc").
     * @return A ResponseEntity containing an ApiResponse with a Page of Employee objects.
     */
    @Operation(summary = "Get employees with filters", description = "Retrieves a paginated list of employees with detailed metadata (totalElements, totalPages, currentPage).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully fetched filtered employees")
    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaginatedResponse<Employee>>> getAllEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        String sortField = sort[0];
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("desc")) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Employee> employeesPage = employeeService.getAllEmployees(department, status, minSalary, maxSalary, includeDeleted, pageable);
        
        PaginatedResponse<Employee> response = PaginatedResponse.from(employeesPage);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", response));
    }

    /**
     * Searches for employees by name.
     *
     * @param name The search query.
     * @param page Page index.
     * @param size Page size.
     * @param sort Sort parameters.
     * @return A Page of Employee objects.
     */
    @Operation(summary = "Search employees by name", description = "Retrieves a paginated list of employees whose names contain the search string, with metadata.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully searched employees")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaginatedResponse<Employee>>> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        String sortField = sort[0];
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("desc")) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Employee> employeesPage = employeeService.searchEmployees(name, department, status, includeDeleted, pageable);
        
        PaginatedResponse<Employee> response = PaginatedResponse.from(employeesPage);
        return ResponseEntity.ok(ApiResponse.success("Employees searched successfully", response));
    }

    @Operation(summary = "Get employee by ID", description = "Retrieves a single employee record by its ID.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Found the employee"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee fetched successfully", employee));
    }

    @Operation(summary = "Create a new employee", description = "Creates a new employee record. Requires ADMIN role.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Employee created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Employee>> createEmployee(@Valid @RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        return new ResponseEntity<>(ApiResponse.success("Employee created successfully", createdEmployee), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an employee", description = "Updates an existing employee record. Requires ADMIN role.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employeeDetails) {
        Employee updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updatedEmployee));
    }

    @Operation(summary = "Delete an employee", description = "Deletes an employee record from the database. Requires ADMIN role.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", response));
    }

    @Operation(summary = "Approve an employee", description = "Sets an employee status to APPROVED. Requires ADMIN role.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee approved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Employee>> approveEmployee(@PathVariable Long id) {
        Employee approvedEmployee = employeeService.approveEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee approved successfully", approvedEmployee));
    }

    @Operation(summary = "Restore an employee", description = "Restores a soft-deleted employee record. Requires ADMIN role.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee restored"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Employee>> restoreEmployee(@PathVariable Long id) {
        Employee restoredEmployee = employeeService.restoreEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee restored successfully", restoredEmployee));
    }
}
