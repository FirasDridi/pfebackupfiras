package com.mss.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId; // Change to UUID

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "amount", nullable = false)
    private double amount;
}
