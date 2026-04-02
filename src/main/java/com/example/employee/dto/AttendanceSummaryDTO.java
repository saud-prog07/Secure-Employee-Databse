package com.example.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryDTO {
    private Long employeeId;
    private String employeeName;
    private Long totalDays;
    private Long presentDays;
    private Long absentDays;
    private Double attendancePercentage;
    private String status;

    public AttendanceSummaryDTO(Long employeeId, String employeeName, Long totalDays, Long presentDays, Double attendancePercentage) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.totalDays = totalDays;
        this.presentDays = presentDays;
        this.absentDays = totalDays - presentDays;
        this.attendancePercentage = attendancePercentage;
        this.status = attendancePercentage >= 75 ? "GOOD" : (attendancePercentage >= 60 ? "AVERAGE" : "POOR");
    }
}
