package com.example.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GroupRevenueStatisticsResponse {
    private Long groupId;
    private String groupName;
    private Map<String, Double> revenueStatistics;
}
