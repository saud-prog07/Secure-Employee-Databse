package com.example.employee.config;

import com.example.employee.entity.*;
import com.example.employee.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;

    public DataLoader(EmployeeRepository employeeRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, 
                      org.springframework.jdbc.core.JdbcTemplate jdbcTemplate, AttendanceRepository attendanceRepository, 
                      PayrollRepository payrollRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.attendanceRepository = attendanceRepository;
        this.payrollRepository = payrollRepository;
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
            ensureHrExists();
            resetAdminPassword();
            resetHrPassword();
            seedEmployees();
            seedAttendanceData();
            seedPayrollData();
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

    private void resetHrPassword() {
        String freshHash = passwordEncoder.encode("hr");
        jdbcTemplate.update(
            "UPDATE users SET password = ?, approved = true, deleted = false WHERE username = 'hr'",
            freshHash
        );
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

    private void ensureHrExists() {
        if (userRepository.findByUsername("hr").isEmpty()) {
            logger.info("HR user not found. Creating default HR user...");
            User hr = new User();
            hr.setUsername("hr");
            hr.setPassword(passwordEncoder.encode("hr"));
            hr.setRole(Role.HR);
            hr.setApproved(true);
            hr.setDeleted(false);
            userRepository.save(hr);
        }
    }

    private void seedEmployees() {
        if (employeeRepository.count() == 0) {
            logger.info("Database is empty. Inserting comprehensive dataset of 20 professional employees...");

            List<Employee> employees = List.of(
                // Engineering Department - Senior and Mid-level Engineers
                new Employee(null, "John Doe", "john.doe@example.com", "Engineering", 95000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Emily Davis", "emily.davis@example.com", "Engineering", 92000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "David Taylor", "david.taylor@example.com", "Engineering", 88000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Jessica Chen", "jessica.chen@example.com", "Engineering", 85000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Marcus Johnson", "marcus.johnson@example.com", "Engineering", 82000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                
                // Sales Department
                new Employee(null, "Michael Wilson", "michael.wilson@example.com", "Sales", 78000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Chris Anderson", "chris.anderson@example.com", "Sales", 75000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Amanda Martinez", "amanda.martinez@example.com", "Sales", 72000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                
                // Finance Department
                new Employee(null, "Sarah Miller", "sarah.miller@example.com", "Finance", 80000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Robert Brown", "robert.brown@example.com", "Finance", 77000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Patricia Lee", "patricia.lee@example.com", "Finance", 74000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                
                // Marketing Department
                new Employee(null, "Jane Smith", "jane.smith@example.com", "Marketing", 72000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Nicole Thompson", "nicole.thompson@example.com", "Marketing", 68000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                
                // HR Department
                new Employee(null, "Karen Rodriguez", "karen.rodriguez@example.com", "HR", 65000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Thomas White", "thomas.white@example.com", "HR", 62000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                
                // Operations Department
                new Employee(null, "James Garcia", "james.garcia@example.com", "Operations", 70000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Lisa Jackson", "lisa.jackson@example.com", "Operations", 67000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                
                // Business Development
                new Employee(null, "William Harris", "william.harris@example.com", "Business Development", 85000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                new Employee(null, "Rachel Green", "rachel.green@example.com", "Business Development", 80000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null),
                
                // Customer Support
                new Employee(null, "Kevin White", "kevin.white@example.com", "Support", 55000.0, false, EmployeeStatus.APPROVED, null, null, false, null, null, null)
            );

            employeeRepository.saveAll(employees);
            logger.info("Successfully inserted {} professional employee records across 8 departments.", employees.size());
        } else {
            logger.info("Database already contains records. Skipping sample data insertion.");
        }
    }

    private void seedAttendanceData() {
        // Add attendance records for all employees with VARIED attendance levels across multiple years
        List<Employee> employees = employeeRepository.findAll();
        if (!employees.isEmpty()) {
            logger.info("Seeding attendance data for {} employees across 2025, 2026, 2027...", employees.size());
            LocalDate today = LocalDate.now(); // April 1, 2026
            
            // Define different attendance percentages for each employee to show varied performance
            double[] attendancePercentages = {0.98, 0.80, 0.64, 0.98, 0.74, 0.84, 0.40, 0.90}; // 98%, 80%, 64%, etc.
            
            int employeeIndex = 0;
            for (Employee employee : employees) {
                double attendancePercentage = employeeIndex < attendancePercentages.length ? attendancePercentages[employeeIndex] : 0.80;
                
                logger.info("Creating attendance records for employee {} with {}% attendance", employee.getName(), 
                    String.format("%.0f", attendancePercentage * 100));
                
                // Seed attendance for 2025 (full year)
                seedAttendanceForYear(employee.getId(), 2025, attendancePercentage);
                
                // Seed attendance for 2026 (Jan 1 to today)
                seedAttendanceForYear(employee.getId(), 2026, attendancePercentage);
                
                // Seed attendance for 2027 (early months - Jan to April)
                LocalDate twentySeven = LocalDate.of(2027, 1, 1);
                LocalDate twentySevenEnd = LocalDate.of(2027, 4, 30);
                seedAttendanceForDateRange(employee.getId(), twentySeven, twentySevenEnd, attendancePercentage);
                
                employeeIndex++;
            }
            logger.info("Successfully seeded attendance data for all years with varied performance levels.");
        }
    }
    
    private void seedAttendanceForYear(Long employeeId, int year, double attendancePercentage) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = year == 2026 ? LocalDate.now() : LocalDate.of(year, 12, 31);
        seedAttendanceForDateRange(employeeId, startOfYear, endOfYear, attendancePercentage);
    }
    
    private void seedAttendanceForDateRange(Long employeeId, LocalDate startDate, LocalDate endDate, double attendancePercentage) {
        int totalWorkingDays = 0;
        int targetPresentDays = 0;
        int currentPresentDays = 0;
        
        // Count total working days first
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int dayOfWeek = current.getDayOfWeek().getValue();
            if (dayOfWeek != 6 && dayOfWeek != 7) { // Not Saturday or Sunday
                totalWorkingDays++;
            }
            current = current.plusDays(1);
        }
        
        targetPresentDays = (int) Math.ceil(totalWorkingDays * attendancePercentage);
        
        // Create attendance records
        current = startDate;
        while (!current.isAfter(endDate)) {
            int dayOfWeek = current.getDayOfWeek().getValue();
            
            // Only process working days (not Saturday=6 or Sunday=7)
            if (dayOfWeek != 6 && dayOfWeek != 7) {
                boolean exists = attendanceRepository.findByEmployeeIdAndDate(employeeId, current).isPresent();
                if (!exists) {
                    boolean shouldBePresent = currentPresentDays < targetPresentDays;
                    
                    if (shouldBePresent) {
                        Attendance attendance = new Attendance();
                        attendance.setEmployeeId(employeeId);
                        attendance.setDate(current);
                        attendance.setLoginTime(LocalDateTime.of(current.getYear(), current.getMonth(), current.getDayOfMonth(), 9, 0, 0));
                        attendance.setStatus(AttendanceStatus.PRESENT);
                        attendanceRepository.save(attendance);
                        currentPresentDays++;
                    }
                }
            }
            current = current.plusDays(1);
        }
    }

    private void seedPayrollData() {
        // Add payroll records for all employees
        List<Employee> employees = employeeRepository.findAll();
        if (!employees.isEmpty()) {
            logger.info("Seeding payroll data for {} employees...", employees.size());
            
            for (Employee employee : employees) {
                // Add 3 months of payroll data
                for (int month = 1; month <= 3; month++) {
                    String monthStr = String.format("2026-%02d", month);
                    
                    // Check if record already exists
                    boolean exists = payrollRepository.findByEmployeeIdAndMonth(employee.getId(), monthStr).isPresent();
                    if (!exists) {
                        Payroll payroll = new Payroll();
                        payroll.setEmployeeId(employee.getId());
                        payroll.setBaseSalary(employee.getSalary());
                        payroll.setBonus((double)(Math.random() * 5000)); // Random bonus 0-5000
                        payroll.setDeductions((double)(Math.random() * 2000)); // Random deductions 0-2000
                        payroll.setMonth(monthStr);
                        double finalSalary = payroll.getBaseSalary() + payroll.getBonus() - payroll.getDeductions();
                        payroll.setFinalSalary(finalSalary);
                        payrollRepository.save(payroll);
                    }
                }
            }
            logger.info("Successfully seeded payroll data.");
        }
    }
}
