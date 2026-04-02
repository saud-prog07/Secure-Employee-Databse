package com.example.employee.controller;

import com.example.employee.dto.WorkdayStatsDTO;
import com.example.employee.dto.ApiResponse;
import com.example.employee.entity.Employee;
import com.example.employee.service.WorkdayService;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workday")
public class WorkdayController {

    private static final Logger logger = LoggerFactory.getLogger(WorkdayController.class);

    @Autowired
    private WorkdayService workdayService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/stats/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse> getWorkdayStatistics(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "2026") Integer year) {
        try {
            WorkdayStatsDTO stats = workdayService.getWorkdayStatistics(employeeId, year);
            return ResponseEntity.ok(ApiResponse.success("Workday statistics retrieved successfully", stats));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve workday statistics: " + e.getMessage()));
        }
    }

    @GetMapping("/current-year")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> getCurrentYear() {
        Map<String, Integer> response = new HashMap<>();
        response.put("currentYear", LocalDate.now().getYear());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<List<WorkdayStatsDTO>>> getAllEmployeeWorkdayStats(
            @RequestParam(defaultValue = "2026") int year) {
        try {
            List<Employee> employees = employeeRepository.findAll();
            logger.info("Found " + (employees != null ? employees.size() : 0) + " employees");
            
            if (employees == null || employees.isEmpty()) {
                logger.warn("No employees found in database");
                return ResponseEntity.ok(ApiResponse.success("No employees found", new ArrayList<>()));
            }
            
            List<WorkdayStatsDTO> stats = new ArrayList<>();
            for (Employee employee : employees) {
                try {
                    WorkdayStatsDTO stat = workdayService.getWorkdayStatistics(employee.getId(), year);
                    stats.add(stat);
                    logger.debug("Added workday stats for employee: " + employee.getName());
                } catch (Exception e) {
                    logger.error("Error getting stats for employee " + employee.getId() + ": " + e.getMessage());
                }
            }
            
            logger.info("Successfully retrieved workday statistics for " + stats.size() + " employees for year " + year);
            return ResponseEntity.ok(ApiResponse.success("Workday statistics retrieved", stats));
        } catch (Exception e) {
            logger.error("Error fetching workday statistics: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("Error fetching statistics: " + e.getMessage()));
        }
    }
}
