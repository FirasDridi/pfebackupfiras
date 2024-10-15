    package com.example.statistics.dto;

    import lombok.AllArgsConstructor;
    import lombok.Data;

    import java.util.Map;

    @Data
    @AllArgsConstructor
    public class UserRevenueStatisticsResponse {
        private String userId;
        private String userName;
        private Map<String, Double> revenueStatistics;
    }
