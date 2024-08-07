package com.mss.demo.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccessLogDTO {
    private String id;
    private Long groupId;
    private String serviceId;
    private String userId;
    private LocalDateTime timestamp;
    private String accessToken;

    public AccessLogDTO(String id, Long groupId, String serviceId, String userId, LocalDateTime timestamp, String accessToken) {
        this.id = id;
        this.groupId = groupId;
        this.serviceId = serviceId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.accessToken = accessToken;
    }
}