package com.example.statistics.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServiceUsageDTO {
    private String serviceName; // You can populate this later based on the endpoint
    private String endpoint;
    private long usageCount;
    private LocalDateTime lastAccessed;
    private String accessToken;
    private String userId;   // Add this if it doesn't exist
    private Long groupId;    // Add this if it doesn't exist
}
