package com.example.statistics.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.YearMonth;
import java.util.List;

@Data
@Entity
@Table(name = "monthly_revenues")
public class MonthlyRevenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private YearMonth month;
    private Double revenueAmount;

    @OneToMany(mappedBy = "monthlyRevenue", cascade = CascadeType.ALL)
    private List<MonthlyServiceUsage> serviceUsages;
}
