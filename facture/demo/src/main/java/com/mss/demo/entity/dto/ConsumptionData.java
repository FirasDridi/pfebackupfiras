package com.mss.demo.entity.dto;

import lombok.Data;

@Data
public class ConsumptionData {
    private Long userId;
    private Long groupId;
    private Long serviceId;
    private Long timestamp;

    // Getters and Setters
}
