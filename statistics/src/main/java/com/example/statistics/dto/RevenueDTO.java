package com.example.statistics.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RevenueDTO {
    private String serviceName;
    private double amount;
    private String userId;
    private Long groupId;
    private LocalDateTime timestamp;
    private double totalRevenue;

}