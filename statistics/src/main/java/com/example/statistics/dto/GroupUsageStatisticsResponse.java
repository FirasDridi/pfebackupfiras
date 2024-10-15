package com.example.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GroupUsageStatisticsResponse {
    private Long groupId;
    private String groupName;
    private Map<String, Long> usageStatistics;
}
