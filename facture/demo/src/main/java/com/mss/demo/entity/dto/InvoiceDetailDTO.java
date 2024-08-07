package com.mss.demo.entity.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class InvoiceDetailDTO {
    private UUID id;
    private UUID userId;
    private Long groupId;
    private String serviceId;
    private String serviceName;
    private LocalDateTime timestamp;
    private double amount;

    public InvoiceDetailDTO(UUID id, UUID userId, Long groupId, String serviceId, String serviceName, LocalDateTime timestamp, double amount) {
        this.id = id;
        this.userId = userId;
        this.groupId = groupId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.timestamp = timestamp;
        this.amount = amount;
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
