package com.example.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class UserUsageStatisticsResponse {
    private String userId;
    private String userName;
    private Map<String, Long> usageStatistics;
}
