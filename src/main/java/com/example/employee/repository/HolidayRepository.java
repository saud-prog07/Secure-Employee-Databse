package com.example.employee.repository;

import com.example.employee.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> findByYear(Integer year);
    
    List<Holiday> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT h FROM Holiday h WHERE YEAR(h.date) = :year ORDER BY h.date")
    List<Holiday> findHolidaysByYear(@Param("year") Integer year);
}
