package com.example.statistics.repository;

import com.example.statistics.Entities.MonthlyServiceUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;

@Repository
public interface MonthlyServiceUsageRepository extends JpaRepository<MonthlyServiceUsage, Long> {
    MonthlyServiceUsage findByServiceNameAndMonth(String serviceName, YearMonth month);
}