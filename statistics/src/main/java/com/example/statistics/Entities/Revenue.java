package com.example.statistics.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "revenues")
public class Revenue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;

    private double amount;

    private String userId;

    private Long groupId;

    private LocalDateTime timestamp;

    private double totalRevenue;
}
