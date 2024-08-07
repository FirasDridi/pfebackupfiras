package com.example.kafkaconsumer.repository;

import com.example.kafkaconsumer.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}
