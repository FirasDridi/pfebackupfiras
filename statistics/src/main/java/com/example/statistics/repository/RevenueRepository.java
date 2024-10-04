package com.example.statistics.repository;

import com.example.statistics.Entities.Revenue;
import com.example.statistics.Entities.ServiceUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Long> {
    // Additional query methods (if needed) can be defined here
}