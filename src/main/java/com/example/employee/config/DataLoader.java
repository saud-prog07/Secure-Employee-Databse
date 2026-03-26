package com.example.employee.config;

import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import com.example.employee.entity.Role;
import com.example.employee.entity.User;
import com.example.employee.repository.EmployeeRepository;
import com.example.employee.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(EmployeeRepository employeeRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Verifying MySQL database connection...");
        try {
            long userCount = userRepository.count();
            long employeeCount = employeeRepository.count();
            logger.info("MySQL Connection SUCCESSFUL. Current stats: {} users, {} employees found.", userCount, employeeCount);
            
            seedUsers();
            seedEmployees();
        } catch (Exception e) {
            logger.error("MySQL Connection FAILED: {}", e.getMessage());
            throw e;
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            logger.info("Database has no users. Inserting default credentials: admin/admin (ADMIN), hr/hr (HR)...");
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(Role.ADMIN);

            User hr = new User();
            hr.setUsername("hr");
            hr.setPassword(passwordEncoder.encode("hr"));
            hr.setRole(Role.HR);

            userRepository.saveAll(Arrays.asList(admin, hr));
            logger.info("Successfully inserted default users: admin and hr.");
        }
    }

    private void seedEmployees() {
        if (employeeRepository.count() == 0) {
            logger.info("Database is empty. Inserting sample employee records...");

            List<Employee> employees = Arrays.asList(
                new Employee(null, "John Doe", "john.doe@example.com", "Engineering", 75000.0, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Jane Smith", "jane.smith@example.com", "Marketing", 65000.0, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Robert Brown", "robert.brown@example.com", "HR", 55000.0, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Emily Davis", "emily.davis@example.com", "Engineering", 80000.0, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Michael Wilson", "michael.wilson@example.com", "Sales", 70000.0, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Sarah Miller", "sarah.miller@example.com", "Finance", 72000.0, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "David Taylor", "david.taylor@example.com", "Engineering", 78000.0, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Chris Anderson", "chris.anderson@example.com", "Sales", 68000.0, EmployeeStatus.APPROVED, null, null)
            );

            employeeRepository.saveAll(employees);
            logger.info("Successfully inserted {} sample employee records.", employees.size());
        } else {
            logger.info("Database already contains records. Skipping sample data insertion.");
        }
    }
}
