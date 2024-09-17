package com.mss.demo.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;


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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}