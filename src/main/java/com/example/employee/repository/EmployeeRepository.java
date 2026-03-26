package com.example.employee.repository;

import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
/**
 * Repository interface for Employee entity.
 * Provides standard JPA operations and custom search methods.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Page<Employee> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
}
