    package com.example.statistics.controller;

    import com.example.statistics.Entities.*;
    import com.example.statistics.dto.GroupRevenueStatisticsResponse;
    import com.example.statistics.dto.GroupUsageStatisticsResponse;
    import com.example.statistics.dto.UserRevenueStatisticsResponse;
    import com.example.statistics.dto.UserUsageStatisticsResponse;
    import com.example.statistics.repository.*;
    import com.example.statistics.service.StatisticsService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.*;

    import java.time.LocalDate;
    import java.time.YearMonth;
    import java.util.List;
    import java.util.Map;

    @CrossOrigin("*")
    @RestController
    @RequestMapping("/api/statistics")
    public class StatisticsController {

        @Autowired
        private StatisticsService statisticsService;

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

        // Existing endpoint for service usage statistics
        @GetMapping("/usage")
        public Map<String, Long> getServiceUsageStatistics() {
            return statisticsService.getServiceUsageStatistics();
        }

        // Existing endpoint for revenue statistics
        @GetMapping("/revenue")
        public Map<String, Double> getRevenueStatistics() {
            return statisticsService.getRevenueStatistics();
        }

        // New endpoint for user usage statistics
        @GetMapping("/user-usage")
        public Map<String, Long> getUserUsageStatistics() {
            return statisticsService.getUserUsageStatistics();
        }

        // New endpoint for group usage statistics
        @GetMapping("/group-usage")
        public Map<String, Long> getGroupUsageStatistics() {
            return statisticsService.getGroupUsageStatistics();
        }

        // New endpoint for user revenue statistics
        @GetMapping("/user-revenue")
        public Map<String, Double> getUserRevenueStatistics() {
            return statisticsService.getUserRevenueStatistics();
        }

        // New endpoint for group revenue statistics
        @GetMapping("/group-revenue")
        public Map<String, Double> getGroupRevenueStatistics() {
            return statisticsService.getGroupRevenueStatistics();
        }

        // Endpoint to fetch persisted ServiceUsage data
        @GetMapping("/persisted-service-usage")
        public List<ServiceUsage> getPersistedServiceUsage() {
            return serviceUsageRepository.findAll();
        }

        // Endpoint to fetch persisted Revenue data
        @GetMapping("/persisted-revenue")
        public List<Revenue> getPersistedRevenue() {
            return revenueRepository.findAll();
        }

        // Endpoint to retrieve all daily service usages
        @GetMapping("/daily-service-usage")
        public List<DailyServiceUsage> getAllDailyServiceUsage() {
            return dailyServiceUsageRepository.findAll();
        }

        // Endpoint to retrieve all monthly service usages
        @GetMapping("/monthly-service-usage")
        public List<MonthlyServiceUsage> getAllMonthlyServiceUsage() {
            return monthlyServiceUsageRepository.findAll();
        }

        // Endpoint to retrieve all daily revenues
        @GetMapping("/daily-revenue")
        public List<DailyRevenue> getAllDailyRevenue() {
            return dailyRevenueRepository.findAll();
        }

        // Endpoint to retrieve all monthly revenues
        @GetMapping("/monthly-revenue")
        public List<MonthlyRevenue> getAllMonthlyRevenue() {
            return monthlyRevenueRepository.findAll();
        }

        // (Optional) Endpoint to retrieve daily service usage for a specific date
        @GetMapping("/daily-service-usage/{serviceName}/{date}")
        public DailyServiceUsage getDailyServiceUsageByDate(@PathVariable String serviceName, @PathVariable String date) {
            LocalDate localDate = LocalDate.parse(date); // Ensure date format is 'YYYY-MM-DD'
            return dailyServiceUsageRepository.findByServiceNameAndDate(serviceName, localDate);
        }
        @GetMapping("/monthly-revenue/{month}")
        public List<MonthlyRevenue> getMonthlyRevenue(@PathVariable String month) {
            YearMonth yearMonth = YearMonth.parse(month);
            return monthlyRevenueRepository.findByMonth(yearMonth);
        }

        @GetMapping("/daily-revenue/{date}")
        public List<DailyRevenue> getDailyRevenue(@PathVariable String date) {
            LocalDate localDate = LocalDate.parse(date);
            return dailyRevenueRepository.findByDate(localDate);
        }
        @GetMapping("/user/{userId}/usage")
        public UserUsageStatisticsResponse getUserUsageStatistics(@PathVariable String userId) {
            return statisticsService.getUserUsageStatisticsResponse(userId);
        }

        @GetMapping("/group/{groupId}/usage")
        public GroupUsageStatisticsResponse getGroupUsageStatistics(@PathVariable Long groupId) {
            return statisticsService.getGroupUsageStatisticsResponse(groupId);
        }

        @GetMapping("/user/{userId}/revenue")
        public UserRevenueStatisticsResponse getUserRevenueStatistics(@PathVariable String userId) {
            return statisticsService.getUserRevenueStatisticsResponse(userId);
        }

        @GetMapping("/group/{groupId}/revenue")
        public GroupRevenueStatisticsResponse getGroupRevenueStatistics(@PathVariable Long groupId) {
            return statisticsService.getGroupRevenueStatisticsResponse(groupId);
        }
    }