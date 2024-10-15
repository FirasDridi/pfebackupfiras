package com.example.statistics.service;

import com.example.statistics.Entities.*;
import com.example.statistics.dto.*;
import com.example.statistics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private DataFetchingService dataFetchingService;

    @Autowired
    private ServiceUsageRepository serviceUsageRepository;

    @Autowired
    private RevenueRepository revenueRepository;

    @Autowired
    private DailyServiceUsageRepository dailyServiceUsageRepository;

    @Autowired
    private MonthlyServiceUsageRepository monthlyServiceUsageRepository;

    @Autowired
    private DailyRevenueRepository dailyRevenueRepository;

    @Autowired
    private MonthlyRevenueRepository monthlyRevenueRepository;

    /**
     * Aggregates and saves service usage data synchronously.
     * @return Map<String, Long>
     */
    public Map<String, Long> getServiceUsageStatistics() {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();

        // Persist the data
        saveServiceUsageData();

        return usageData.stream()
                .collect(Collectors.groupingBy(usage -> {
                    String serviceName = dataFetchingService.fetchServiceNameByToken(usage.getAccessToken());
                    return (serviceName != null && !serviceName.isEmpty()) ? serviceName : usage.getEndpoint();
                }, Collectors.counting()));
    }

    /**
     * Aggregates and saves revenue data synchronously.
     * @return Map<String, Double>
     */
    public Map<String, Double> getRevenueStatistics() {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();

        // Persist the data
        saveRevenueData();

        return revenueData.stream()
                .collect(Collectors.groupingBy(RevenueDTO::getServiceName, Collectors.summingDouble(RevenueDTO::getAmount)));
    }

    /**
     * Aggregates usage statistics by user name synchronously.
     * @return Map<String, Long>
     */
    public Map<String, Long> getUserUsageStatistics() {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();
        return usageData.stream()
                .collect(Collectors.groupingBy(
                        usage -> dataFetchingService.fetchUserNameById(usage.getUserId()),
                        Collectors.counting()
                ));
    }

    /**
     * Aggregates usage statistics by group name synchronously.
     * @return Map<String, Long>
     */
    public Map<String, Long> getGroupUsageStatistics() {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();
        return usageData.stream()
                .collect(Collectors.groupingBy(
                        usage -> dataFetchingService.fetchGroupNameById(usage.getGroupId()),
                        Collectors.counting()
                ));
    }

    /**
     * Aggregates revenue statistics by user name synchronously.
     * @return Map<String, Double>
     */
    public Map<String, Double> getUserRevenueStatistics() {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();
        return revenueData.stream()
                .collect(Collectors.groupingBy(
                        revenue -> dataFetchingService.fetchUserNameById(revenue.getUserId()),
                        Collectors.summingDouble(RevenueDTO::getAmount)
                ));
    }

    /**
     * Aggregates revenue statistics by group name synchronously.
     * @return Map<String, Double>
     */
    public Map<String, Double> getGroupRevenueStatistics() {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();
        return revenueData.stream()
                .collect(Collectors.groupingBy(
                        revenue -> dataFetchingService.fetchGroupNameById(revenue.getGroupId()),
                        Collectors.summingDouble(RevenueDTO::getAmount)
                ));
    }

    /**
     * Saves service usage data to the database synchronously.
     */
    public void saveServiceUsageData() {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();
        List<ServiceUsage> serviceUsages = usageData.stream()
                .map(dto -> {
                    ServiceUsage usage = new ServiceUsage();
                    usage.setServiceName(dto.getServiceName());
                    usage.setEndpoint(dto.getEndpoint());
                    usage.setUsageCount(dto.getUsageCount());
                    usage.setLastAccessed(dto.getLastAccessed());
                    usage.setAccessToken(dto.getAccessToken());
                    usage.setUserId(dto.getUserId());
                    usage.setGroupId(dto.getGroupId());
                    return usage;
                })
                .collect(Collectors.toList());

        serviceUsageRepository.saveAll(serviceUsages);
    }

    /**
     * Saves revenue data to the database synchronously.
     */
    public void saveRevenueData() {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();
        List<Revenue> revenues = revenueData.stream()
                .map(dto -> {
                    Revenue revenue = new Revenue();
                    revenue.setServiceName(dto.getServiceName());
                    revenue.setAmount(dto.getAmount());
                    revenue.setUserId(dto.getUserId());
                    revenue.setGroupId(dto.getGroupId());
                    revenue.setTimestamp(dto.getTimestamp());
                    revenue.setTotalRevenue(dto.getTotalRevenue());
                    return revenue;
                })
                .collect(Collectors.toList());

        revenueRepository.saveAll(revenues);
    }

    /**
     * Aggregates daily service usage and saves to the database synchronously.
     */
    public void aggregateDailyServiceUsage() {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();
        LocalDate today = LocalDate.now();

        // Filter data for today
        List<ServiceUsageDTO> todaysUsage = usageData.stream()
                .filter(usage -> usage.getLastAccessed() != null && usage.getLastAccessed().toLocalDate().isEqual(today))
                .collect(Collectors.toList());

        // Enrich usage data with user and group names
        List<EnrichedServiceUsage> enrichedUsage = todaysUsage.stream()
                .map(usage -> new EnrichedServiceUsage(
                        usage.getServiceName(),
                        usage.getEndpoint(),
                        usage.getUsageCount(),
                        usage.getLastAccessed().toLocalDate(),
                        dataFetchingService.fetchUserNameById(usage.getUserId()),
                        dataFetchingService.fetchGroupNameById(usage.getGroupId())
                ))
                .collect(Collectors.toList());

        // Group by service name and count
        Map<String, Long> usageByService = enrichedUsage.stream()
                .collect(Collectors.groupingBy(EnrichedServiceUsage::getServiceName, Collectors.counting()));

        // Save to DailyServiceUsageRepository
        usageByService.forEach((serviceName, count) -> {
            DailyServiceUsage dailyUsage = dailyServiceUsageRepository.findByServiceNameAndDate(serviceName, today);
            if (dailyUsage == null) {
                dailyUsage = new DailyServiceUsage();
                dailyUsage.setServiceName(serviceName);
                dailyUsage.setDate(today);
            }
            dailyUsage.setUsageCount(count);
            dailyServiceUsageRepository.save(dailyUsage);
        });
    }

    /**
     * Aggregates monthly service usage and saves to the database synchronously.
     */
    public void aggregateMonthlyServiceUsage() {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();
        YearMonth currentMonth = YearMonth.now();

        // Filter data for current month
        List<ServiceUsageDTO> monthlyUsage = usageData.stream()
                .filter(usage -> usage.getLastAccessed() != null &&
                        YearMonth.from(usage.getLastAccessed()).equals(currentMonth))
                .collect(Collectors.toList());

        // Enrich usage data with user and group names
        List<EnrichedServiceUsage> enrichedUsage = monthlyUsage.stream()
                .map(usage -> new EnrichedServiceUsage(
                        usage.getServiceName(),
                        usage.getEndpoint(),
                        usage.getUsageCount(),
                        usage.getLastAccessed().toLocalDate(),
                        dataFetchingService.fetchUserNameById(usage.getUserId()),
                        dataFetchingService.fetchGroupNameById(usage.getGroupId())
                ))
                .collect(Collectors.toList());

        // Group by service name and count
        Map<String, Long> usageByService = enrichedUsage.stream()
                .collect(Collectors.groupingBy(EnrichedServiceUsage::getServiceName, Collectors.counting()));

        // Save to MonthlyServiceUsageRepository
        usageByService.forEach((serviceName, count) -> {
            MonthlyServiceUsage monthlyUsageEntity = monthlyServiceUsageRepository.findByServiceNameAndMonth(serviceName, currentMonth);
            if (monthlyUsageEntity == null) {
                monthlyUsageEntity = new MonthlyServiceUsage();
                monthlyUsageEntity.setServiceName(serviceName);
                monthlyUsageEntity.setMonth(currentMonth);
            }
            monthlyUsageEntity.setUsageCount(count);
            monthlyServiceUsageRepository.save(monthlyUsageEntity);
        });
    }

    /**
     * Aggregates daily revenue and saves to the database synchronously.
     */
    public void aggregateDailyRevenue() {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();
        LocalDate today = LocalDate.now();

        // Filter data for today
        List<RevenueDTO> todaysRevenue = revenueData.stream()
                .filter(revenue -> revenue.getTimestamp() != null && revenue.getTimestamp().toLocalDate().isEqual(today))
                .collect(Collectors.toList());

        // Enrich revenue data with user and group names
        List<EnrichedRevenue> enrichedRevenue = todaysRevenue.stream()
                .map(revenue -> new EnrichedRevenue(
                        revenue.getServiceName(),
                        revenue.getAmount(),
                        dataFetchingService.fetchUserNameById(revenue.getUserId()),
                        dataFetchingService.fetchGroupNameById(revenue.getGroupId()),
                        revenue.getTimestamp(),
                        revenue.getTotalRevenue()
                ))
                .collect(Collectors.toList());

        // Group by service name and sum amounts
        Map<String, Double> revenueByService = enrichedRevenue.stream()
                .collect(Collectors.groupingBy(EnrichedRevenue::getServiceName, Collectors.summingDouble(EnrichedRevenue::getAmount)));

        // Save to DailyRevenueRepository
        revenueByService.forEach((serviceName, amount) -> {
            DailyRevenue dailyRevenue = dailyRevenueRepository.findByServiceNameAndDate(serviceName, today);
            if (dailyRevenue == null) {
                dailyRevenue = new DailyRevenue();
                dailyRevenue.setServiceName(serviceName);
                dailyRevenue.setDate(today);
            }
            dailyRevenue.setRevenueAmount(amount);
            dailyRevenueRepository.save(dailyRevenue);
        });
    }

    /**
     * Aggregates monthly revenue and saves to the database synchronously.
     */
    public void aggregateMonthlyRevenue() {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();
        YearMonth currentMonth = YearMonth.now();

        // Filter data for current month
        List<RevenueDTO> monthlyRevenue = revenueData.stream()
                .filter(revenue -> revenue.getTimestamp() != null &&
                        YearMonth.from(revenue.getTimestamp()).equals(currentMonth))
                .collect(Collectors.toList());

        // Enrich revenue data with user and group names
        List<EnrichedRevenue> enrichedRevenue = monthlyRevenue.stream()
                .map(revenue -> new EnrichedRevenue(
                        revenue.getServiceName(),
                        revenue.getAmount(),
                        dataFetchingService.fetchUserNameById(revenue.getUserId()),
                        dataFetchingService.fetchGroupNameById(revenue.getGroupId()),
                        revenue.getTimestamp(),
                        revenue.getTotalRevenue()
                ))
                .collect(Collectors.toList());

        // Group by service name and sum amounts
        Map<String, Double> revenueByService = enrichedRevenue.stream()
                .collect(Collectors.groupingBy(EnrichedRevenue::getServiceName, Collectors.summingDouble(EnrichedRevenue::getAmount)));

        // Save to MonthlyRevenueRepository
        revenueByService.forEach((serviceName, amount) -> {
            MonthlyRevenue monthlyRevenueEntity = monthlyRevenueRepository.findByServiceNameAndMonth(serviceName, currentMonth);
            if (monthlyRevenueEntity == null) {
                monthlyRevenueEntity = new MonthlyRevenue();
                monthlyRevenueEntity.setServiceName(serviceName);
                monthlyRevenueEntity.setMonth(currentMonth);
            }
            monthlyRevenueEntity.setRevenueAmount(amount);
            monthlyRevenueRepository.save(monthlyRevenueEntity);
        });
    }

    // New method to get user-specific usage statistics
    public Map<String, Long> getUserUsageStatistics(String userId) {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();
        return usageData.stream()
                .filter(usage -> usage.getUserId().equals(userId))
                .collect(Collectors.groupingBy(
                        usage -> dataFetchingService.fetchServiceNameByToken(usage.getAccessToken()),
                        Collectors.counting()
                ));
    }

    // New method to get group-specific usage statistics
    public Map<String, Long> getGroupUsageStatistics(Long groupId) {
        List<ServiceUsageDTO> usageData = dataFetchingService.fetchServiceUsageData();
        return usageData.stream()
                .filter(usage -> usage.getGroupId().equals(groupId))
                .collect(Collectors.groupingBy(
                        usage -> dataFetchingService.fetchServiceNameByToken(usage.getAccessToken()),
                        Collectors.counting()
                ));
    }

    // New method to get user-specific revenue statistics
    public Map<String, Double> getUserRevenueStatistics(String userId) {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();
        return revenueData.stream()
                .filter(revenue -> revenue.getUserId().equals(userId))
                .collect(Collectors.groupingBy(
                        RevenueDTO::getServiceName,
                        Collectors.summingDouble(RevenueDTO::getAmount)
                ));
    }

    // New method to get group-specific revenue statistics
    public Map<String, Double> getGroupRevenueStatistics(Long groupId) {
        List<RevenueDTO> revenueData = dataFetchingService.fetchRevenueData();
        return revenueData.stream()
                .filter(revenue -> revenue.getGroupId().equals(groupId))
                .collect(Collectors.groupingBy(
                        RevenueDTO::getServiceName,
                        Collectors.summingDouble(RevenueDTO::getAmount)
                ));
    }

    /**
     * Inner class to represent aggregated service usage.
     */
    private static class EnrichedServiceUsage {
        private String serviceName;
        private String endpoint;
        private long usageCount;
        private LocalDate date;
        private String userName;
        private String groupName;

        public EnrichedServiceUsage(String serviceName, String endpoint, long usageCount, LocalDate date, String userName, String groupName) {
            this.serviceName = serviceName;
            this.endpoint = endpoint;
            this.usageCount = usageCount;
            this.date = date;
            this.userName = userName;
            this.groupName = groupName;
        }

        public String getServiceName() {
            return serviceName;
        }

        // Getters and setters can be added as needed
    }

    /**
     * Inner class to represent aggregated revenue.
     */
    private static class EnrichedRevenue {
        private String serviceName;
        private double amount;
        private String userName;
        private String groupName;
        private LocalDateTime timestamp;
        private double totalRevenue;

        public EnrichedRevenue(String serviceName, double amount, String userName, String groupName, LocalDateTime timestamp, double totalRevenue) {
            this.serviceName = serviceName;
            this.amount = amount;
            this.userName = userName;
            this.groupName = groupName;
            this.timestamp = timestamp;
            this.totalRevenue = totalRevenue;
        }

        public String getServiceName() {
            return serviceName;
        }

        public double getAmount() {
            return amount;
        }

        // Getters and setters can be added as needed
    }

    /**
     * Returns user usage statistics along with the user name.
     */
    public UserUsageStatisticsResponse getUserUsageStatisticsResponse(String userId) {
        String userName = dataFetchingService.fetchUserNameById(userId);
        Map<String, Long> usageStatistics = getUserUsageStatistics(userId);
        return new UserUsageStatisticsResponse(userId, userName, usageStatistics);
    }

    /**
     * Returns group usage statistics along with the group name.
     */
    public GroupUsageStatisticsResponse getGroupUsageStatisticsResponse(Long groupId) {
        String groupName = dataFetchingService.fetchGroupNameById(groupId);
        Map<String, Long> usageStatistics = getGroupUsageStatistics(groupId);
        return new GroupUsageStatisticsResponse(groupId, groupName, usageStatistics);
    }

    /**
     * Returns user revenue statistics along with the user name.
     */
    public UserRevenueStatisticsResponse getUserRevenueStatisticsResponse(String userId) {
        String userName = dataFetchingService.fetchUserNameById(userId);
        Map<String, Double> revenueStatistics = getUserRevenueStatistics(userId);
        return new UserRevenueStatisticsResponse(userId, userName, revenueStatistics);
    }

    /**
     * Returns group revenue statistics along with the group name.
     */
    public GroupRevenueStatisticsResponse getGroupRevenueStatisticsResponse(Long groupId) {
        String groupName = dataFetchingService.fetchGroupNameById(groupId);
        Map<String, Double> revenueStatistics = getGroupRevenueStatistics(groupId);
        return new GroupRevenueStatisticsResponse(groupId, groupName, revenueStatistics);
    }
}
