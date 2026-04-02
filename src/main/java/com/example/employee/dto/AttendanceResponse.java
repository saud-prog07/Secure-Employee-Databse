package com.example.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private LocalDateTime loginTime;
    private String status;
    private String message;

    public AttendanceResponse(Long employeeId, String employeeName, LocalDateTime loginTime, String message) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.loginTime = loginTime;
        this.message = message;
        this.date = LocalDate.now();
    }
}
