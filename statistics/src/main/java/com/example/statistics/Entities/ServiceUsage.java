package com.example.statistics.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "service_usages")
public class ServiceUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;

    private String endpoint;

    private long usageCount;

    private LocalDateTime lastAccessed;

    private String accessToken;

    private String userId;

    private Long groupId;
}
