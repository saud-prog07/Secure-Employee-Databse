package com.example.employee.controller;

import com.example.employee.dto.ApiResponse;
import com.example.employee.dto.AttendanceRequest;
import com.example.employee.dto.AttendanceResponse;
import com.example.employee.dto.AttendanceSummaryDTO;
import com.example.employee.entity.Attendance;
import com.example.employee.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<AttendanceResponse>> scanAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        try {
            AttendanceResponse response = attendanceService.scanAttendance(request);
            return ResponseEntity.ok(ApiResponse.success("Attendance scanned successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status/{employeeId}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceStatus(
            @PathVariable String employeeId) {
        try {
            AttendanceResponse response = attendanceService.getAttendanceStatus(employeeId);
            return ResponseEntity.ok(ApiResponse.success("Attendance status retrieved", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/history/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<ApiResponse<List<Attendance>>> getEmployeeHistory(
            @PathVariable Long employeeId) {
        try {
            List<Attendance> history = attendanceService.getEmployeeAttendanceHistory(employeeId);
            return ResponseEntity.ok(ApiResponse.success("Attendance history retrieved", history));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<ApiResponse<List<Attendance>>> getTodayAttendance() {
        List<Attendance> todayAttendance = attendanceService.getTodayAttendance();
        return ResponseEntity.ok(ApiResponse.success("Today's attendance retrieved", todayAttendance));
    }

    @GetMapping("/range/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendanceRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Attendance> attendance = attendanceService.getEmployeeAttendanceRange(employeeId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Attendance range retrieved", attendance));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/summary/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<ApiResponse<AttendanceSummaryDTO>> getAttendanceSummary(
            @PathVariable Long employeeId) {
        try {
            AttendanceSummaryDTO summary = attendanceService.getAttendanceSummary(employeeId);
            return ResponseEntity.ok(ApiResponse.success("Attendance summary retrieved", summary));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/summary/{employeeId}/month/{yearMonth}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<ApiResponse<AttendanceSummaryDTO>> getMonthlyAttendanceSummary(
            @PathVariable Long employeeId,
            @PathVariable String yearMonth) {
        try {
            YearMonth month = YearMonth.parse(yearMonth);
            AttendanceSummaryDTO summary = attendanceService.getMonthlyAttendanceSummary(employeeId, month);
            return ResponseEntity.ok(ApiResponse.success("Monthly attendance summary retrieved", summary));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid year-month format. Use YYYY-MM"));
        }
    }
}
