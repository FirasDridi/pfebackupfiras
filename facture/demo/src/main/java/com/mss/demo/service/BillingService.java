package com.mss.demo.service;

import com.mss.demo.entity.Invoice;
import com.mss.demo.entity.dto.AccessLogDTO;
import com.mss.demo.entity.dto.InvoiceDetailDTO;
import com.mss.demo.entity.dto.ServiceDTO;
import com.mss.demo.entity.dto.UserDTO;
import com.mss.demo.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public void generateInvoices() {
        logger.info("Starting invoice generation process...");

        List<AccessLogDTO> accessLogs = fetchAccessLogs();
        if (accessLogs == null || accessLogs.isEmpty()) {
            logger.warn("No access logs found.");
            return;
        }

        // Fetch user details and create a map
        Map<String, UserDTO> userMap = accessLogs.stream()
                .map(this::fetchUserDetails)
                .filter(user -> user != null && user.getKeycloakId() != null)
                .collect(Collectors.toMap(UserDTO::getKeycloakId, user -> user, (existing, replacement) -> existing));

        // Fetch service details and create a map
        Map<String, ServiceDTO> serviceMap = accessLogs.stream()
                .map(this::fetchServiceDetails)
                .filter(service -> service != null && service.getAccessToken() != null)
                .collect(Collectors.toMap(ServiceDTO::getAccessToken, service -> service, (existing, replacement) -> existing));

        // Process each access log
        for (AccessLogDTO accessLog : accessLogs) {
            UserDTO user = userMap.get(accessLog.getUserId());
            ServiceDTO service = serviceMap.get(accessLog.getAccessToken());

            if (user == null) {
                logger.warn("User not found for user ID: {}", accessLog.getUserId());
                continue;
            }

            if (service == null) {
                logger.warn("Service not found for access token: {}", accessLog.getAccessToken());
                continue;
            }

            double amount = calculateAmount(service, 1); // Assuming 1 usage per log entry
            saveInvoice(user, accessLog, amount);
        }

        logger.info("Invoice generation process completed.");
    }

    public List<AccessLogDTO> fetchAccessLogs() {
        try {
            return webClientBuilder.baseUrl("http://localhost:8033")
                    .build()
                    .get()
                    .uri("/api/v1/consumption/logs")
                    .retrieve()
                    .bodyToFlux(AccessLogDTO.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            logger.error("Error fetching access logs: {}", e.getMessage(), e);
            if (e.getStatusCode().is5xxServerError()) {
                throw new RuntimeException("The consumption service is currently unavailable. Please try again later.");
            }
            throw new RuntimeException("An unexpected error occurred while fetching access logs.");
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred while fetching access logs.");
        }
    }

    private UserDTO fetchUserDetails(AccessLogDTO accessLog) {
        String userUri = "http://localhost:8884/admin/getUserByKeycloakId/" + accessLog.getUserId();
        try {
            logger.info("Fetching user details from URI: {}", userUri);
            UserDTO user = webClientBuilder.build()
                    .get()
                    .uri(userUri)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
            if (user == null || user.getKeycloakId() == null) {
                logger.warn("User details returned null or incomplete for user ID: {}", accessLog.getUserId());
                return null;
            } else {
                logger.info("Fetched user details: {}", user);
                return user;
            }
        } catch (Exception e) {
            logger.error("Error fetching user details for user ID: {} from URI: {}", accessLog.getUserId(), userUri, e);
            return null;
        }
    }

    private ServiceDTO fetchServiceDetails(AccessLogDTO accessLog) {
        String serviceUri = "http://localhost:8081/service/api/services/access-token/" + accessLog.getAccessToken();
        try {
            logger.info("Fetching service details from URI: {}", serviceUri);
            ServiceDTO service = webClientBuilder.build()
                    .get()
                    .uri(serviceUri)
                    .retrieve()
                    .bodyToMono(ServiceDTO.class)
                    .block();
            if (service == null) {
                logger.warn("Service details returned null for access token: {}", accessLog.getAccessToken());
            }
            return service;
        } catch (Exception e) {
            logger.error("Error fetching service details for access token: {}", accessLog.getAccessToken(), e);
            return null;
        }
    }


    private double calculateAmount(ServiceDTO service, long usageCount) {
        return Double.parseDouble(service.getPricing()) * usageCount;
    }

    private void saveInvoice(UserDTO user, AccessLogDTO accessLog, double amount) {
        LocalDateTime timestamp = accessLog.getTimestamp();

        // Check if an invoice already exists based on userId, groupId, and timestamp
        Optional<Invoice> existingInvoice = invoiceRepository.findByUserIdAndGroupIdAndTimestamp(
                UUID.fromString(user.getKeycloakId()),
                accessLog.getGroupId(),
                timestamp
        );

        if (existingInvoice.isPresent()) {
            logger.info("Invoice already exists for user ID: {}, group ID: {}, timestamp: {}",
                    user.getKeycloakId(), accessLog.getGroupId(), timestamp);
            return;
        }

        ServiceDTO service = fetchServiceDetails(accessLog);
        if (service == null) {
            logger.warn("Service details not found for access token: {}", accessLog.getAccessToken());
            return;
        }

        Invoice invoice = new Invoice();
        invoice.setUserId(UUID.fromString(user.getKeycloakId())); // Assuming user.getKeycloakId() returns a UUID string
        invoice.setGroupId(accessLog.getGroupId()); // Use the groupId from AccessLogDTO
        invoice.setServiceId(service.getId()); // Use the service ID from the service details
        invoice.setTimestamp(timestamp);
        invoice.setAmount(amount);
        invoiceRepository.save(invoice);
        logger.info("Saved invoice: {}", invoice);
    }


    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<InvoiceDetailDTO> getUserInvoices(String userId) {
        return invoiceRepository.findByUserId(UUID.fromString(userId)).stream()
                .map(this::toInvoiceDetailDTO)
                .collect(Collectors.toList());
    }

    public List<InvoiceDetailDTO> getGroupInvoices(Long groupId) {
        return invoiceRepository.findByGroupId(groupId).stream()
                .map(this::toInvoiceDetailDTO)
                .collect(Collectors.toList());
    }

    // BillingService.java

    public double getTotalAmountForUser(UUID userId) {
        return invoiceRepository.findByUserId(userId).stream()
                .mapToDouble(Invoice::getAmount)
                .sum();
    }



    public double getTotalAmountForGroup(Long groupId) {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.getGroupId().equals(groupId))
                .mapToDouble(Invoice::getAmount)
                .sum();
    }

    public long getUserServiceUsageCount(String userId, UUID serviceId) {
        return invoiceRepository.findByUserId(UUID.fromString(userId)).stream()
                .filter(invoice -> invoice.getServiceId().equals(serviceId))
                .count();
    }

    public InvoiceDetailDTO toInvoiceDetailDTO(Invoice invoice) {
        ServiceDTO service = fetchServiceDetails(new AccessLogDTO(invoice.getId().toString(), invoice.getGroupId(), null, invoice.getUserId().toString(), invoice.getTimestamp(), invoice.getServiceId().toString()));
        String serviceName = service != null ? service.getName() : "Unknown Service";
        return new InvoiceDetailDTO(invoice.getId(), invoice.getUserId(), invoice.getGroupId(), invoice.getServiceId().toString(), serviceName, invoice.getTimestamp(), invoice.getAmount());
    }
}
