package com.mss.servicemanager.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionRequestDTO {
    private Long groupId;
    private UUID serviceId;
}
