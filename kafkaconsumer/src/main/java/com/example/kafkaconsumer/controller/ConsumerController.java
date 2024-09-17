package com.example.kafkaconsumer.controller;

import com.example.kafkaconsumer.entity.AccessLog;
import com.example.kafkaconsumer.repository.AccessLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consumption")
@CrossOrigin("*")
public class ConsumerController {

    @Autowired
    private AccessLogRepository accessLogRepository;

    @GetMapping("/logs")
    public List<AccessLog> getAllAccessLogs() {
        return accessLogRepository.findAll();
    }
    @GetMapping("/user/{userId}/services")
    public List<AccessLog> getServicesAccessedByUser(@PathVariable String userId) {
        return accessLogRepository.findByUserId(userId);
    }
}
