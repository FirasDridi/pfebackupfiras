    package com.mss.demo.service;
    
    import com.mss.demo.entity.Invoice;
    import com.mss.demo.entity.dto.*;
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
    
        // Main method to generate invoices
        public void generateInvoices() {
            logger.info("Starting invoice generation process...");
    
            // Fetch access logs from the consumption service
            List<AccessLogDTO> accessLogs = fetchAccessLogs();
            if (accessLogs == null || accessLogs.isEmpty()) {
                logger.warn("No access logs found.");
                return;
            }
    
            // Generate invoices for users and groups
            generateUserInvoices(accessLogs);
            generateGroupInvoices(accessLogs);
    
            logger.info("Invoice generation process completed.");
        }
    
        // Fetch all access logs from the consumption service
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
    
        // Fetch user details from the admin service
        private UserDTO fetchUserDetails(AccessLogDTO accessLog) {
            String userUri = "http://localhost:8884/admin/getUser/" + accessLog.getUserId();
            try {
                logger.info("Fetching user details from URI: {}", userUri);
                UserDTO user = webClientBuilder.build()
                        .get()
                        .uri(userUri)
                        .retrieve()
                        .bodyToMono(UserDTO.class)
                        .block();
                if (user == null) {
                    logger.warn("User details returned null for user ID: {}", accessLog.getUserId());
                    return null;
                } else {
                    return user;
                }
            } catch (Exception e) {
                logger.error("Error fetching user details for user ID: {} from URI: {}", accessLog.getUserId(), userUri, e);
                return null;
            }
        }
    
        // Fetch service details from the service manager
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
                    return null;
                }

                logger.info("Fetched service details: {}", service);
                return service;
            } catch (WebClientResponseException e) {
                if (e.getStatusCode().is4xxClientError()) {
                    logger.warn("Service not found for access token: {}", accessLog.getAccessToken());
                } else {
                    logger.error("Error fetching service details for access token: {}", accessLog.getAccessToken(), e);
                }
                return null;
            } catch (Exception e) {
                logger.error("Unexpected error fetching service details for access token: {}", accessLog.getAccessToken(), e);
                return null;
            }
        }


        // Fetch group details from the admin service
        private GroupDTO fetchGroupDetails(Long groupId) {
            String groupUri = "http://localhost:8884/api/v1/groups/details/" + groupId;
            try {
                logger.info("Fetching group details from URI: {}", groupUri);
                GroupDTO group = webClientBuilder.build()
                        .get()
                        .uri(groupUri)
                        .retrieve()
                        .bodyToMono(GroupDTO.class)
                        .block();
                if (group == null || group.getId() == null) {
                    logger.warn("Group details returned null or incomplete for group ID: {}", groupId);
                    return null;
                } else {
                    logger.info("Fetched group details: {}", group);
                    return group;
                }
            } catch (Exception e) {
                logger.error("Error fetching group details for group ID: {} from URI: {}", groupId, groupUri, e);
                return null;
            }
        }
    
        // Calculate the amount based on service pricing and usage count
        private double calculateAmount(ServiceDTO service, long usageCount) {
            return Double.parseDouble(service.getPricing()) * usageCount;
        }
    
        // Save invoice to the repository
        private void saveInvoice(UserDTO user, AccessLogDTO accessLog, double amount) {
            LocalDateTime timestamp = accessLog.getTimestamp();

            // Check if an invoice already exists based on userId, groupId, and timestamp
            Optional<Invoice> existingInvoice = invoiceRepository.findByUserIdAndGroupIdAndTimestamp(
                    UUID.fromString(user.getId()),
                    accessLog.getGroupId(),
                    timestamp
            );

            if (existingInvoice.isPresent()) {
                logger.info("Invoice already exists for user ID: {}, group ID: {}, timestamp: {}",
                        user.getId(), accessLog.getGroupId(), timestamp);
                return;
            }

            ServiceDTO service = fetchServiceDetails(accessLog);
            if (service == null) {
                logger.warn("Service details not found for access token: {}", accessLog.getAccessToken());
                return;
            }

            Invoice invoice = new Invoice();
            invoice.setUserId(UUID.fromString(user.getId())); // Assuming user.getKeycloakId() returns a UUID string
            invoice.setGroupId(accessLog.getGroupId()); // Use the groupId from AccessLogDTO
            invoice.setServiceId(service.getId()); // Use the service ID from the service details
            invoice.setServiceName(service.getName()); // Store the service name directly in the invoice
            invoice.setTimestamp(timestamp);
            invoice.setAmount(amount);
            invoiceRepository.save(invoice);
            logger.info("Saved invoice: {}", invoice);
        }


        // Generate invoices for each user based on access logs
        public void generateUserInvoices(List<AccessLogDTO> accessLogs) {
            logger.info("Generating invoices for users...");
    
            // Map userId to UserDTO
            Map<String, UserDTO> userMap = accessLogs.stream()
                    .map(this::fetchUserDetails)
                    .filter(user -> user != null && user.getId() != null)
                    .collect(Collectors.toMap(UserDTO::getId, user -> user, (existing, replacement) -> existing));
    
            for (String userId : userMap.keySet()) {
                List<AccessLogDTO> userLogs = accessLogs.stream()
                        .filter(log -> userId.equals(log.getUserId()))
                        .collect(Collectors.toList());
    
                double totalAmount = 0.0;
                for (AccessLogDTO log : userLogs) {
                    ServiceDTO service = fetchServiceDetails(log);
                    if (service == null) {
                        logger.warn("Service not found for access token: {}", log.getAccessToken());
                        continue;
                    }
                    double amount = calculateAmount(service, 1); // Assuming 1 usage per log entry
                    totalAmount += amount;
                    saveInvoice(userMap.get(userId), log, amount);
                }
    
                logger.info("Total amount for user ID {}: {}", userId, totalAmount);
            }
        }
    
        // Generate invoices for each group based on access logs
        public void generateGroupInvoices(List<AccessLogDTO> accessLogs) {
            logger.info("Generating invoices for groups...");
    
            // Map groupId to GroupDTO
            Map<Long, GroupDTO> groupMap = accessLogs.stream()
                    .map(log -> fetchGroupDetails(log.getGroupId()))
                    .filter(group -> group != null && group.getId() != null)
                    .collect(Collectors.toMap(GroupDTO::getId, group -> group, (existing, replacement) -> existing));
    
            for (Long groupId : groupMap.keySet()) {
                List<AccessLogDTO> groupLogs = accessLogs.stream()
                        .filter(log -> groupId.equals(log.getGroupId()))
                        .collect(Collectors.toList());
    
                double totalAmount = 0.0;
                for (AccessLogDTO log : groupLogs) {
                    ServiceDTO service = fetchServiceDetails(log);
                    if (service == null) {
                        logger.warn("Service not found for access token: {}", log.getAccessToken());
                        continue;
                    }
                    double amount = calculateAmount(service, 1); // Assuming 1 usage per log entry
                    totalAmount += amount;
                    UserDTO user = fetchUserDetails(log);
                    saveInvoice(user, log, amount);
                }
    
                logger.info("Total amount for group ID {}: {}", groupId, totalAmount);
            }
        }
    
        // Retrieve all invoices from the repository
        public List<Invoice> getAllInvoices() {
            return invoiceRepository.findAll();
        }
    
        // Retrieve invoices for a specific user
        public List<InvoiceDetailDTO> getUserInvoices(String userId) {
            return invoiceRepository.findByUserId(UUID.fromString(userId)).stream()
                    .map(this::toInvoiceDetailDTO)
                    .collect(Collectors.toList());
        }
    
        // Retrieve invoices for a specific group
        public List<InvoiceDetailDTO> getGroupInvoices(Long groupId) {
            return invoiceRepository.findByGroupId(groupId).stream()
                    .map(this::toInvoiceDetailDTO)
                    .collect(Collectors.toList());
        }
    
        // Calculate the total amount for a specific user
        public double getTotalAmountForUser(UUID userId) {
            return invoiceRepository.findByUserId(userId).stream()
                    .mapToDouble(Invoice::getAmount)
                    .sum();
        }
    
        // Calculate the total amount for a specific group
        public double getTotalAmountForGroup(Long groupId) {
            return invoiceRepository.findAll().stream()
                    .filter(invoice -> invoice.getGroupId().equals(groupId))
                    .mapToDouble(Invoice::getAmount)
                    .sum();
        }
    
        // Map Invoice entity to InvoiceDetailDTO
        public InvoiceDetailDTO toInvoiceDetailDTO(Invoice invoice) {
            String serviceName = invoice.getServiceName(); // Directly use the stored serviceName

            GroupDTO group = fetchGroupDetails(invoice.getGroupId());
            String groupName = group != null ? group.getName() : "Unknown Group";

            UserDTO user = fetchUserDetails(new AccessLogDTO(
                    invoice.getId().toString(),
                    invoice.getGroupId(),
                    invoice.getServiceId().toString(),
                    invoice.getUserId().toString(),
                    invoice.getTimestamp(),
                    invoice.getServiceId().toString()
            ));
            String userName = user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown User";

            return new InvoiceDetailDTO(
                    invoice.getId(),
                    invoice.getUserId(),
                    invoice.getGroupId(),
                    invoice.getServiceId().toString(),
                    serviceName,
                    invoice.getTimestamp(),
                    invoice.getAmount(),
                    groupName,
                    userName
            );
        }



    }
