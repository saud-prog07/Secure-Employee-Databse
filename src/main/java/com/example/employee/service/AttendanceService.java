package com.example.employee.service;

import com.example.employee.entity.Attendance;
import com.example.employee.entity.Employee;
import com.example.employee.dto.AttendanceRequest;
import com.example.employee.dto.AttendanceResponse;
import com.example.employee.dto.AttendanceSummaryDTO;
import com.example.employee.repository.AttendanceRepository;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    public AttendanceResponse scanAttendance(AttendanceRequest request) {
        String employeeIdStr = request.getEmployeeId();

        Employee employee = employeeRepository.findByEmployeeId(employeeIdStr)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeIdStr));

        Long employeeId = employee.getId();

        LocalDate today = LocalDate.now();

        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);

        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
            String formattedTime = attendance.getLoginTime().format(formatter);
            
            auditLogService.logAction(
                    "ATTENDANCE_SCAN_DUPLICATE",
                    "SYSTEM"
            );

            return new AttendanceResponse(
                    employeeId,
                    employee.getName(),
                    attendance.getLoginTime(),
                    "Already logged in at " + formattedTime
            );
        }

        LocalDateTime loginTime = LocalDateTime.now();
        Attendance newAttendance = new Attendance(employeeId, today, loginTime);
        attendanceRepository.save(newAttendance);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedTime = loginTime.format(formatter);

        auditLogService.logAction(
                "ATTENDANCE_SCAN",
                "SYSTEM"
        );

        return new AttendanceResponse(
                employeeId,
                employee.getName(),
                loginTime,
                "Logged in at " + formattedTime
        );
    }

    public AttendanceResponse getAttendanceStatus(String employeeIdStr) {
        Employee employee = employeeRepository.findByEmployeeId(employeeIdStr)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeIdStr));

        Long employeeId = employee.getId();
        LocalDate today = LocalDate.now();

        Optional<Attendance> attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);

        if (attendance.isPresent()) {
            Attendance record = attendance.get();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
            String formattedTime = record.getLoginTime().format(formatter);

            return new AttendanceResponse(
                    employeeId,
                    employee.getName(),
                    record.getLoginTime(),
                    "Logged in at " + formattedTime
            );
        }

        return new AttendanceResponse(
                employeeId,
                employee.getName(),
                null,
                "NOT LOGGED IN"
        );
    }

    public List<Attendance> getEmployeeAttendanceHistory(Long employeeId) {
        return attendanceRepository.findByEmployeeIdOrderByDateDesc(employeeId);
    }

    public List<Attendance> getTodayAttendance() {
        return attendanceRepository.findByDateOrderByLoginTimeAsc(LocalDate.now());
    }

    public List<Attendance> getEmployeeAttendanceRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);
    }

    public AttendanceSummaryDTO getAttendanceSummary(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        List<Attendance> allAttendance = attendanceRepository.findByEmployeeIdOrderByDateDesc(employeeId);

        if (allAttendance.isEmpty()) {
            return new AttendanceSummaryDTO(
                    employeeId,
                    employee.getName(),
                    0L,
                    0L,
                    0.0
            );
        }

        long totalDays = allAttendance.size();
        long presentDays = allAttendance.stream()
                .filter(a -> a.getStatus() != null && a.getStatus().toString().equals("PRESENT"))
                .count();

        double attendancePercentage = totalDays > 0 ? (presentDays * 100.0 / totalDays) : 0.0;
        attendancePercentage = Math.round(attendancePercentage * 100.0) / 100.0;

        auditLogService.logAction(
                "ATTENDANCE_SUMMARY",
                "SYSTEM"
        );

        return new AttendanceSummaryDTO(
                employeeId,
                employee.getName(),
                totalDays,
                presentDays,
                attendancePercentage
        );
    }

    public AttendanceSummaryDTO getMonthlyAttendanceSummary(Long employeeId, YearMonth yearMonth) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Attendance> monthlyAttendance = attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);

        if (monthlyAttendance.isEmpty()) {
            return new AttendanceSummaryDTO(
                    employeeId,
                    employee.getName(),
                    0L,
                    0L,
                    0.0
            );
        }

        long totalDays = monthlyAttendance.size();
        long presentDays = monthlyAttendance.stream()
                .filter(a -> a.getStatus() != null && a.getStatus().toString().equals("PRESENT"))
                .count();

        double attendancePercentage = totalDays > 0 ? (presentDays * 100.0 / totalDays) : 0.0;
        attendancePercentage = Math.round(attendancePercentage * 100.0) / 100.0;

        return new AttendanceSummaryDTO(
                employeeId,
                employee.getName(),
                totalDays,
                presentDays,
                attendancePercentage
        );
    }
}
