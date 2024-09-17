package com.example.statistics.controller;

import com.example.statistics.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@CrossOrigin("*")

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    // Existing endpoint for service usage statistics
    @GetMapping("/usage")
    public Map<String, Long> getServiceUsageStatistics() {
        return statisticsService.getServiceUsageStatistics();
    }

    // Existing endpoint for revenue statistics
    @GetMapping("/revenue")
    public Map<String, Double> getRevenueStatistics() {
        return statisticsService.getRevenueStatistics();
    }

    // New endpoint for user usage statistics
    @GetMapping("/user-usage")
    public Map<String, Long> getUserUsageStatistics() {
        return statisticsService.getUserUsageStatistics();
    }

    // New endpoint for group usage statistics
    @GetMapping("/group-usage")
    public Map<Long, Long> getGroupUsageStatistics() {
        return statisticsService.getGroupUsageStatistics();
    }

    // New endpoint for user revenue statistics
    @GetMapping("/user-revenue")
    public Map<String, Double> getUserRevenueStatistics() {
        return statisticsService.getUserRevenueStatistics();
    }

    // New endpoint for group revenue statistics
    @GetMapping("/group-revenue")
    public Map<Long, Double> getGroupRevenueStatistics() {
        return statisticsService.getGroupRevenueStatistics();
    }
}
