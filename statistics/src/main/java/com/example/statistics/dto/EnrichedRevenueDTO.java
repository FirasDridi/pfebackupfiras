package com.example.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EnrichedRevenueDTO {
    private String serviceName;
    private double revenueAmount;
    private String userName;
    private String groupName;
    private LocalDateTime timestamp;
    private double totalRevenue;
}