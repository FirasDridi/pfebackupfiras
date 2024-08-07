package com.example.kafkaconsumer.controller;

import com.example.kafkaconsumer.entity.AccessLog;
import com.example.kafkaconsumer.repository.AccessLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consumption")
public class ConsumerController {

    @Autowired
    private AccessLogRepository accessLogRepository;

    @GetMapping("/logs")
    public List<AccessLog> getAllAccessLogs() {
        return accessLogRepository.findAll();
    }
}
