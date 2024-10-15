package com.example.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EnrichedServiceUsageDTO {
    private String serviceName;
    private String endpoint;
    private long usageCount;
    private LocalDate lastAccessed;
    private String userName;
    private String groupName;
}