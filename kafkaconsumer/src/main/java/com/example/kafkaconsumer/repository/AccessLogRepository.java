package com.example.kafkaconsumer.repository;

import com.example.kafkaconsumer.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    List<AccessLog> findByUserId(String userId);

}
