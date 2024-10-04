package com.example.statistics.repository;

import com.example.statistics.Entities.ServiceUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceUsageRepository extends JpaRepository<ServiceUsage, Long> {
    // Additional query methods (if needed) can be defined here
}