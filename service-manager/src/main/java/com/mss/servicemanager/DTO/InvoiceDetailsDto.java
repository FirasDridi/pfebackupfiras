package com.mss.servicemanager.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class InvoiceDetailsDto {
    private UUID id;
    private UUID userId;
    private Long groupId;
    private UUID serviceId;
    private String serviceName;
    private String timestamp;
    private Double amount;
    private String userName;
    private String groupName;
}
