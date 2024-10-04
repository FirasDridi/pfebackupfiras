package com.example.statistics.repository;

import com.example.statistics.Entities.DailyServiceUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DailyServiceUsageRepository extends JpaRepository<DailyServiceUsage, Long> {
    DailyServiceUsage findByServiceNameAndDate(String serviceName, LocalDate date);
}