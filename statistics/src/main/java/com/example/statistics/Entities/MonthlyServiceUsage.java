package com.example.statistics.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.YearMonth;

@Data@Entity
@Table(name = "monthly_service_usages")
public class MonthlyServiceUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private YearMonth month;
    private Long usageCount;

    @ManyToOne
    @JoinColumn(name = "revenue_id")
    private MonthlyRevenue monthlyRevenue;
}
