package com.example.employee.service;

import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.employee.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for Employee business logic.
 * Handles database interactions and data transformation.
 */
@Service
@Slf4j
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Creates a new employee record in the database.
     * New employees always start in PENDING status.
     *
     * @param employee The employee object to be created.
     * @return The saved employee object.
     */
    public Employee createEmployee(Employee employee) {
        log.info("Creating new employee with email: {}. Initial status: PENDING", employee.getEmail());
        employee.setStatus(EmployeeStatus.PENDING);
        return employeeRepository.save(employee);
    }

    /**
     * Retrieves a paginated list of all APPROVED employees.
     *
     * @param pageable Pagination information (page number, size, sort).
     * @return A Page of Employee objects.
     */
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findByStatus(EmployeeStatus.APPROVED, pageable);
    }

    /**
     * Searches for employees by name with pagination.
     *
     * @param name     The name (or part of the name) to search for.
     * @param pageable Pagination information.
     * @return A Page of matching Employee objects.
     */
    public Page<Employee> searchEmployees(String name, Pageable pageable) {
        log.info("Searching for employees with name containing: '{}'", name);
        return employeeRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    /**
     * Retrieves a single employee record by ID.
     *
     * @param id The ID of the employee to retrieve.
     * @return The found Employee object.
     * @throws ResourceNotFoundException if the employee is not found.
     */
    public Employee getEmployeeById(Long id) {
        log.debug("Fetching employee with id: {}", id);
        return employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee not found with id: {}", id);
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
     * Deletes an employee record from the database.
     *
     * @param id The ID of the employee to delete.
     */
    public void deleteEmployee(Long id) {
        log.info("Attempting to delete employee with id: {}", id);
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
        log.info("Employee with id: {} deleted successfully", id);
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
        log.info("Employee with id: {} status updated to APPROVED", id);
        return approvedEmployee;
    }
}
