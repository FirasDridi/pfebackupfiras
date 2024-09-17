package com.example.statistics.service;

import com.example.statistics.dto.RevenueDTO;
import com.example.statistics.dto.ServiceUsageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private DataFetchingService dataFetchingService;

    // Existing method for service usage statistics
    public Map<String, Long> getServiceUsageStatistics() {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();
        return usageData.stream()
                .collect(Collectors.groupingBy(usage -> {
                    String serviceName = dataFetchingService.fetchServiceNameByToken(usage.getAccessToken());
                    return (serviceName != null && !serviceName.isEmpty()) ? serviceName : usage.getEndpoint();
                }, Collectors.counting()));
    }

    // Existing method for revenue statistics
    public Map<String, Double> getRevenueStatistics() {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();
        return revenueData.stream()
                .collect(Collectors.groupingBy(RevenueDTO::getServiceName, Collectors.summingDouble(RevenueDTO::getAmount)));
    }

    // New method to get usage statistics by user
    public Map<String, Long> getUserUsageStatistics() {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();
        return usageData.stream()
                .collect(Collectors.groupingBy(ServiceUsageDTO::getUserId, Collectors.counting()));
    }

    // New method to get usage statistics by group
    public Map<Long, Long> getGroupUsageStatistics() {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();
        return usageData.stream()
                .collect(Collectors.groupingBy(ServiceUsageDTO::getGroupId, Collectors.counting()));
    }

    // New method to get revenue statistics by user
    public Map<String, Double> getUserRevenueStatistics() {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();
        return revenueData.stream()
                .collect(Collectors.groupingBy(RevenueDTO::getUserId, Collectors.summingDouble(RevenueDTO::getAmount)));
    }

    // New method to get revenue statistics by group
    public Map<Long, Double> getGroupRevenueStatistics() {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();
        return revenueData.stream()
                .collect(Collectors.groupingBy(RevenueDTO::getGroupId, Collectors.summingDouble(RevenueDTO::getAmount)));
    }
}
