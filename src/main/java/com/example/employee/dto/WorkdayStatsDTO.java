package com.example.employee.dto;

public class WorkdayStatsDTO {
    private Long employeeId;
    private String employeeName;
    private String emailAddress;
    private String department;
    private Integer totalWorkdaysInYear;
    private Integer presentDays;
    private Integer absentDays;
    private Double attendancePercentage;
    private String status;
    private Integer weekendDays;
    private Integer holidayDays;
    private Integer year;

    public WorkdayStatsDTO() {}

    public WorkdayStatsDTO(Long employeeId, String employeeName, String emailAddress, String department,
                          Integer totalWorkdaysInYear, Integer presentDays, Integer absentDays,
                          Double attendancePercentage, String status, Integer weekendDays,
                          Integer holidayDays, Integer year) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.emailAddress = emailAddress;
        this.department = department;
        this.totalWorkdaysInYear = totalWorkdaysInYear;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.attendancePercentage = attendancePercentage;
        this.status = status;
        this.weekendDays = weekendDays;
        this.holidayDays = holidayDays;
        this.year = year;
    }

    // Getters and Setters
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getTotalWorkdaysInYear() {
        return totalWorkdaysInYear;
    }

    public void setTotalWorkdaysInYear(Integer totalWorkdaysInYear) {
        this.totalWorkdaysInYear = totalWorkdaysInYear;
    }

    public Integer getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(Integer presentDays) {
        this.presentDays = presentDays;
    }

    public Integer getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(Integer absentDays) {
        this.absentDays = absentDays;
    }

    public Double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getWeekendDays() {
        return weekendDays;
    }

    public void setWeekendDays(Integer weekendDays) {
        this.weekendDays = weekendDays;
    }

    public Integer getHolidayDays() {
        return holidayDays;
    }

    public void setHolidayDays(Integer holidayDays) {
        this.holidayDays = holidayDays;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
