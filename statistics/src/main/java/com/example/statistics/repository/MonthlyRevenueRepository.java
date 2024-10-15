package com.example.statistics.repository;

import com.example.statistics.Entities.MonthlyRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;

@Repository
public interface MonthlyRevenueRepository extends JpaRepository<MonthlyRevenue, Long> {
    MonthlyRevenue findByServiceNameAndMonth(String serviceName, YearMonth month);
    List<MonthlyRevenue> findByMonth(YearMonth month);

}