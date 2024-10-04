package com.example.statistics.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.YearMonth;

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
}