package com.example.employee.service;

import com.example.employee.dto.WorkdayStatsDTO;
import com.example.employee.entity.Attendance;
import com.example.employee.entity.Employee;
import com.example.employee.entity.Holiday;
import com.example.employee.repository.AttendanceRepository;
import com.example.employee.repository.EmployeeRepository;
import com.example.employee.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkdayService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    public WorkdayStatsDTO getWorkdayStatistics(Long employeeId, Integer year) {
        // Verify employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Get all holidays for the year
        List<Holiday> holidays = holidayRepository.findByYear(year);
        Set<LocalDate> holidayDates = holidays.stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());

        // Calculate total workdays (excluding weekends and holidays)
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        // For current year, calculate to today; for past/future years, calculate to end of year
        LocalDate endDate = LocalDate.now();
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        
        // If requested year is not the current year, use end of that year
        if (year != endDate.getYear()) {
            endDate = endOfYear;
        }
        
        // If requested year is before current year, we want full year
        if (year < endDate.getYear()) {
            endDate = endOfYear;
        }
        
        int totalWorkdaysInYear = calculateWorkdays(startOfYear, endDate, holidayDates);

        // Get attendance records for the year
        List<Attendance> attendanceRecords = attendanceRepository.findByEmployeeIdAndDateBetween(
                employeeId, startOfYear, endOfYear);

        // Count present days
        int presentDays = (int) attendanceRecords.stream()
                .filter(a -> "PRESENT".equals(a.getStatus().toString()))
                .count();

        // Calculate absent days (total workdays - present days)
        int absentDays = totalWorkdaysInYear - presentDays;

        // Calculate percentage
        Double attendancePercentage = totalWorkdaysInYear > 0 
                ? (presentDays * 100.0 / totalWorkdaysInYear) 
                : 0.0;

        // Determine status
        String status = getStatus(attendancePercentage);

        // Return DTO
        return new WorkdayStatsDTO(
                employeeId,
                employee.getName(),
                employee.getEmail(),
                employee.getDepartment(),
                totalWorkdaysInYear,
                presentDays,
                absentDays,
                Math.round(attendancePercentage * 100.0) / 100.0,
                status,
                countWeekends(startOfYear, endDate),
                holidays.size(),
                year
        );
    }

    // Calculate workdays excluding weekends and holidays
    private int calculateWorkdays(LocalDate start, LocalDate end, Set<LocalDate> holidays) {
        int workdays = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            // Exclude Saturdays (SATURDAY = 6) and Sundays (SUNDAY = 7)
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                // Exclude holidays
                if (!holidays.contains(current)) {
                    workdays++;
                }
            }
            current = current.plusDays(1);
        }

        return workdays;
    }

    // Count total weekend days in the year
    private int countWeekends(LocalDate start, LocalDate end) {
        int weekendDays = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                weekendDays++;
            }
            current = current.plusDays(1);
        }

        return weekendDays;
    }

    // Determine performance status based on attendance percentage
    private String getStatus(Double percentage) {
        if (percentage >= 90) {
            return "EXCELLENT";
        } else if (percentage >= 75) {
            return "GOOD";
        } else if (percentage >= 60) {
            return "AVERAGE";
        } else {
            return "POOR";
        }
    }
}
