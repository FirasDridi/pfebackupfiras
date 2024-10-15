package com.example.statistics.scheduler;

import com.example.statistics.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StatisticsScheduler {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsScheduler.class);

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Scheduled task to aggregate daily service usage at 1 AM every day.
     */
    @Scheduled(cron = "0 0 1 * * ?") // At 1 AM daily
    public void scheduleDailyServiceUsageAggregation() {
        logger.info("Starting daily service usage aggregation...");
        statisticsService.aggregateDailyServiceUsage();
        logger.info("Completed daily service usage aggregation.");
    }

    /**
     * Scheduled task to aggregate monthly service usage at 2 AM on the first day of every month.
     */
    @Scheduled(cron = "0 0 2 1 * ?") // At 2 AM on the 1st day of every month
    public void scheduleMonthlyServiceUsageAggregation() {
        logger.info("Starting monthly service usage aggregation...");
        statisticsService.aggregateMonthlyServiceUsage();
        logger.info("Completed monthly service usage aggregation.");
    }

    /**
     * Scheduled task to aggregate daily revenue at 3 AM every day.
     */
    @Scheduled(cron = "0 0 3 * * ?") // At 3 AM daily
    public void scheduleDailyRevenueAggregation() {
        logger.info("Starting daily revenue aggregation...");
        statisticsService.aggregateDailyRevenue();
        logger.info("Completed daily revenue aggregation.");
    }

    /**
     * Scheduled task to aggregate monthly revenue at 4 AM on the first day of every month.
     */
    @Scheduled(cron = "0 0 4 1 * ?") // At 4 AM on the 1st day of every month
    public void scheduleMonthlyRevenueAggregation() {
        logger.info("Starting monthly revenue aggregation...");
        statisticsService.aggregateMonthlyRevenue();
        logger.info("Completed monthly revenue aggregation.");
    }

}
