package com.example.employee.service;

import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import com.example.employee.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.employee.specification.EmployeeSpecification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for Employee business logic.
 * Handles database interactions and data transformation.
 */
@Service
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    public EmployeeService(EmployeeRepository employeeRepository, AuditLogService auditLogService) {
        this.employeeRepository = employeeRepository;
        this.auditLogService = auditLogService;
    }

    /**
     * Creates a new employee record in the database.
     * New employees always start in PENDING status.
     *
     * @param employee The employee object to be created.
     * @return The saved employee object.
     */
    public Employee createEmployee(Employee employee) {
        String username = getCurrentUsername();
        boolean isAdmin = isCurrentUserAdmin();
        EmployeeStatus initialStatus = isAdmin ? EmployeeStatus.APPROVED : EmployeeStatus.PENDING;

        log.info("Creating new employee by user: {} (isAdmin: {}). Assigned status: {}", 
                username, isAdmin, initialStatus);

        employee.setStatus(initialStatus);
        Employee savedEmployee = employeeRepository.save(employee);
        
        // Log auditing action
        auditLogService.logAction("CREATE_EMPLOYEE", username);
        
        return savedEmployee;
    }

    /**
     * Retrieves a paginated list of employees based on dynamic filters.
     *
     * @param department Optional department filter.
     * @param status     Optional status filter (defaults to APPROVED if not specified).
     * @param minSalary  Optional minimum salary filter.
     * @param maxSalary  Optional maximum salary filter.
     * @param pageable   Pagination information.
     * @return A Page of Employee objects.
     */
    public Page<Employee> getAllEmployees(String department, EmployeeStatus status, Double minSalary, Double maxSalary, Boolean includeDeleted, Pageable pageable) {
        log.info("Fetching employees with filters - Dept: {}, Status: {}, MinSalary: {}, MaxSalary: {}, IncludeDeleted: {}", 
                department, status, minSalary, maxSalary, includeDeleted);

        Specification<Employee> spec = Specification.where(Boolean.TRUE.equals(includeDeleted) ? null : EmployeeSpecification.isNotDeleted())
                .and(EmployeeSpecification.hasDepartment(department))
                .and(EmployeeSpecification.hasStatus(status))
                .and(EmployeeSpecification.hasMinSalary(minSalary))
                .and(EmployeeSpecification.hasMaxSalary(maxSalary));

        return employeeRepository.findAll(spec, pageable);
    }

    /**
     * Searches for employees with multi-criteria filtering.
     *
     * @param name       Partial match for name.
     * @param department Optional department filter.
     * @param status     Optional status filter.
     * @param pageable   Pagination information.
     * @return A Page of matching Employee objects.
     */
    public Page<Employee> searchEmployees(String name, String department, EmployeeStatus status, Boolean includeDeleted, Pageable pageable) {
        log.info("Searching employees - Name: {}, Dept: {}, Status: {}, IncludeDeleted: {}", name, department, status, includeDeleted);
        
        Specification<Employee> spec = Specification.where(Boolean.TRUE.equals(includeDeleted) ? null : EmployeeSpecification.isNotDeleted())
                .and(EmployeeSpecification.hasDepartment(department))
                .and(EmployeeSpecification.hasStatus(status))
                .and((root, query, cb) -> name == null ? null : 
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
                
        return employeeRepository.findAll(spec, pageable);
    }

    /**
     * Retrieves a single employee record by ID.
     *
     * @param id The ID of the employee to retrieve.
     * @return The found Employee object.
     * @throws ResourceNotFoundException if the employee is not found.
     */
    public Employee getEmployeeById(Long id) {
        log.debug("Fetching employee with id: {} (must not be soft-deleted)", id);
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    log.error("Active employee not found with id: {}", id);
                    return new ResourceNotFoundException("Employee not found with id: " + id);
                });
    }

    /**
     * Updates an existing employee record.
     *
     * @param id              The ID of the employee to update.
     * @param employeeDetails The new details for the employee.
     * @return The updated Employee object.
     */
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        log.info("Updating employee with id: {}", id);
        Employee employee = getEmployeeById(id);
        
        employee.setName(employeeDetails.getName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setSalary(employeeDetails.getSalary());
        employee.setStatus(employeeDetails.getStatus());
        
        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employee with id: {} updated successfully", id);
        return updatedEmployee;
    }

    /**
     * Performs a soft-delete on an employee record.
     * The record is marked as deleted by setting the isDeleted flag to true,
     * ensuring it is excluded from all future fetch and search queries.
     *
     * @param id The ID of the employee to soft-delete.
     */
    public void deleteEmployee(Long id) {
        log.info("Performing soft-delete for employee with id: {}", id);
        Employee employee = getEmployeeById(id);
        
        // Mark as deleted instead of removing from DB
        employee.setDeleted(true);
        employeeRepository.save(employee);
        
        // Log auditing action
        auditLogService.logAction("DELETE_EMPLOYEE", getCurrentUsername());
        
        log.info("Employee with id: {} soft-deleted successfully", id);
    }

    /**
     * Restores a soft-deleted employee record.
     *
     * @param id The ID of the employee to restore.
     * @return The restored Employee object.
     */
    public Employee restoreEmployee(Long id) {
        log.info("Restoring soft-deleted employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        
        if (!employee.isDeleted()) {
            throw new IllegalArgumentException("Employee is not deleted.");
        }
        
        employee.setDeleted(false);
        Employee restoredEmployee = employeeRepository.save(employee);
        
        // Log auditing action
        auditLogService.logAction("RESTORE_EMPLOYEE", getCurrentUsername());
        
        log.info("Employee with id: {} restored successfully", id);
        return restoredEmployee;
    }

    /**
     * Approves a pending employee record.
     *
     * @param id The ID of the employee to approve.
     * @return The updated Employee object with APPROVED status.
     */
    public Employee approveEmployee(Long id) {
        log.info("Approving employee with id: {}", id);
        Employee employee = getEmployeeById(id);
        employee.setStatus(EmployeeStatus.APPROVED);
        Employee approvedEmployee = employeeRepository.save(employee);
        
        // Log auditing action
        auditLogService.logAction("APPROVE_EMPLOYEE", getCurrentUsername());
        
        log.info("Employee with id: {} status updated to APPROVED", id);
        return approvedEmployee;
    }

    /**
     * Helper method to get the currently logged-in username.
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymousUser";
    }

    /**
     * Helper method to check if the currently logged-in user is an admin.
     */
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }
}
