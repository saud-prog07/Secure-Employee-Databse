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


import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DataLoader(EmployeeRepository employeeRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Verifying MySQL database connection...");
        try {
            long userCount = userRepository.count();
            long employeeCount = employeeRepository.count();
            logger.info("MySQL Connection SUCCESSFUL. Current stats: {} users, {} employees found.", userCount, employeeCount);
            
            // Fix legacy schema constraints
            try {
                jdbcTemplate.execute("ALTER TABLE audit_logs DROP COLUMN employee_id");
                logger.info("Schema cleanup: Dropped 'employee_id' from 'audit_logs'.");
            } catch (Exception e) {
                logger.debug("Schema cleanup: 'employee_id' already dropped or table not found.");
            }
            try {
                jdbcTemplate.execute("ALTER TABLE audit_logs DROP COLUMN performed_by");
                logger.info("Schema cleanup: Dropped 'performed_by' from 'audit_logs'.");
            } catch (Exception e) {
                logger.debug("Schema cleanup: 'performed_by' already dropped or table not found.");
            }

            // Ensure database is preserved across restarts by not dropping records indiscriminately
            ensureAdminExists();
            resetAdminPassword();
            seedEmployees();
        } catch (Exception e) {
            logger.error("MySQL Connection FAILED: {}", e.getMessage());
            throw e;
        }
    }

    private void resetAdminPassword() {
        // Generate fresh hash at runtime (not hardcoded) and write directly via SQL
        String freshHash = passwordEncoder.encode("admin");
        System.out.println("===== Generated hash: " + freshHash + " =====");
        System.out.println("===== Pre-save verify: " + passwordEncoder.matches("admin", freshHash) + " =====");

        int rows = jdbcTemplate.update(
            "UPDATE users SET password = ?, approved = true, deleted = false WHERE username = 'admin'",
            freshHash
        );
        if (rows > 0) {
            // Read back from DB to confirm the save
            String savedHash = jdbcTemplate.queryForObject(
                "SELECT password FROM users WHERE username = 'admin'", String.class);
            System.out.println("===== Saved hash in DB: " + savedHash + " =====");
            System.out.println("===== Post-save verify: " + passwordEncoder.matches("admin", savedHash) + " =====");
        } else {
            System.out.println("===== WARNING: No admin user found to reset =====");
        }
    }

    private void ensureAdminExists() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            logger.info("Admin user not found. Creating default admin user...");
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(Role.ADMIN);
            admin.setApproved(true);
            admin.setDeleted(false);

            userRepository.save(admin);
            logger.info("Successfully inserted default admin user.");
        } else {
            logger.info("Admin user already exists. Skipping insertion.");
        }
    }

    private void seedEmployees() {
        if (employeeRepository.count() == 0) {
            logger.info("Database is empty. Inserting sample employee records...");

            List<Employee> employees = List.of(
                new Employee(null, "John Doe", "john.doe@example.com", "Engineering", 75000.0, false, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Jane Smith", "jane.smith@example.com", "Marketing", 65000.0, false, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Robert Brown", "robert.brown@example.com", "HR", 55000.0, false, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Emily Davis", "emily.davis@example.com", "Engineering", 80000.0, false, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Michael Wilson", "michael.wilson@example.com", "Sales", 70000.0, false, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Sarah Miller", "sarah.miller@example.com", "Finance", 72000.0, false, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "David Taylor", "david.taylor@example.com", "Engineering", 78000.0, false, EmployeeStatus.APPROVED, null, null),
                new Employee(null, "Chris Anderson", "chris.anderson@example.com", "Sales", 68000.0, false, EmployeeStatus.APPROVED, null, null)
            );

            employeeRepository.saveAll(employees);
            logger.info("Successfully inserted {} sample employee records.", employees.size());
        } else {
            logger.info("Database already contains records. Skipping sample data insertion.");
        }
    }
}
