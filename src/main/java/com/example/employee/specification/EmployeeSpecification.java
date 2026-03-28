package com.example.employee.specification;

import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Utility class for building dynamic JPA Specifications for the Employee entity.
 */
public class EmployeeSpecification {

    public static Specification<Employee> hasDepartment(String department) {
        return (root, query, cb) -> 
            department == null ? null : cb.equal(root.get("department"), department);
    }

    public static Specification<Employee> hasStatus(EmployeeStatus status) {
        return (root, query, cb) -> 
            status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Employee> hasMinSalary(Double minSalary) {
        return (root, query, cb) -> 
            minSalary == null ? null : cb.greaterThanOrEqualTo(root.get("salary"), minSalary);
    }

    public static Specification<Employee> hasMaxSalary(Double maxSalary) {
        return (root, query, cb) -> 
            maxSalary == null ? null : cb.lessThanOrEqualTo(root.get("salary"), maxSalary);
    }

    public static Specification<Employee> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }
}
