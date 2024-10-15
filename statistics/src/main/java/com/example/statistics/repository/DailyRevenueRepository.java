package com.example.statistics.repository;

import com.example.statistics.Entities.DailyRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyRevenueRepository extends JpaRepository<DailyRevenue, Long> {
    DailyRevenue findByServiceNameAndDate(String serviceName, LocalDate date);
    List<DailyRevenue> findByDate(LocalDate date);

}