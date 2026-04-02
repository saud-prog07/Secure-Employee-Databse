package com.example.employee.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

/**
 * Service to read and integrate with Windows system calendar holidays.
 * Reads US Federal Holidays which are system-wide on Windows.
 */
@Service
public class WindowsCalendarService {

    /**
     * Get all US Federal Holidays for a given year.
     * These are system holidays recognized by Windows calendar.
     */
    public Set<LocalDate> getSystemHolidays(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        // Fixed holidays
        holidays.add(LocalDate.of(year, Month.JANUARY, 1));      // New Year's Day
        holidays.add(LocalDate.of(year, Month.JULY, 4));         // Independence Day
        holidays.add(LocalDate.of(year, Month.NOVEMBER, 11));    // Veterans Day
        holidays.add(LocalDate.of(year, Month.DECEMBER, 25));    // Christmas

        // Fixed date holidays
        holidays.add(LocalDate.of(year, Month.JANUARY, 15));     // MLK Day (3rd Monday)
        holidays.add(LocalDate.of(year, Month.FEBRUARY, 19));    // Presidents Day (3rd Monday)
        holidays.add(LocalDate.of(year, Month.MAY, 27));         // Memorial Day (last Monday)
        holidays.add(LocalDate.of(year, Month.SEPTEMBER, 2));    // Labor Day (1st Monday)
        holidays.add(LocalDate.of(year, Month.OCTOBER, 14));     // Columbus Day (2nd Monday)
        holidays.add(LocalDate.of(year, Month.NOVEMBER, 28));    // Thanksgiving (4th Thursday)

        return holidays;
    }

    /**
     * Calculate weekdays between two dates (excluding weekends).
     * This represents actual working days in the calendar.
     */
    public int countWeekdays(LocalDate startDate, LocalDate endDate) {
        int weekdayCount = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (isWeekday(currentDate)) {
                weekdayCount++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return weekdayCount;
    }

    /**
     * Check if a date is a weekday (Monday-Friday).
     */
    private boolean isWeekday(LocalDate date) {
        return date.getDayOfWeek().getValue() <= 5;  // Monday=1 to Friday=5
    }

    /**
     * Get the actual system time (useful for real-time calculations).
     */
    public LocalDate getCurrentSystemDate() {
        return LocalDate.now();
    }

    /**
     * Calculate workdays considering weekends and system holidays.
     */
    public int calculateWorkdaysWithHolidays(LocalDate startDate, LocalDate endDate, int year) {
        Set<LocalDate> systemHolidays = getSystemHolidays(year);
        int workdayCount = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // Count if it's a weekday AND not a system holiday
            if (isWeekday(currentDate) && !systemHolidays.contains(currentDate)) {
                workdayCount++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return workdayCount;
    }
}
