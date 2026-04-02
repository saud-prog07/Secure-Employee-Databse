package com.example.employee.repository;

import com.example.employee.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId);

    List<Attendance> findByDateOrderByLoginTimeAsc(LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :employeeId AND a.date = CURRENT_DATE")
    Optional<Attendance> findTodayAttendance(@Param("employeeId") Long employeeId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employeeId = :employeeId AND a.date = :date AND a.status = 'PRESENT'")
    long countPresentToday(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
}
