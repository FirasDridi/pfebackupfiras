package com.example.statistics.service;

import com.example.statistics.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializationRunner implements CommandLineRunner {

    @Autowired
    private StatisticsService statisticsService;

    @Override
    public void run(String... args) throws Exception {
        // Aggregate monthly data on startup
        statisticsService.aggregateMonthlyServiceUsage();
        statisticsService.aggregateMonthlyRevenue();
    }
}
