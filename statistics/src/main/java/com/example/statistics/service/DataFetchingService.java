package com.example.statistics.service;

import com.example.statistics.dto.RevenueDTO;
import com.example.statistics.dto.ServiceUsageDTO;
import com.example.statistics.dto.UserDTO;
import com.example.statistics.dto.UserGroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class DataFetchingService {

    private static final Logger logger = LoggerFactory.getLogger(DataFetchingService.class);

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Fetches service usage data synchronously.
     * @return List<ServiceUsageDTO>
     */
    public List<ServiceUsageDTO> fetchServiceUsageData() {
        try {
            logger.info("Fetching service usage data from /api/v1/consumption/logs");

            ResponseEntity<List<ServiceUsageDTO>> responseEntity = restTemplate.exchange(
                    "http://localhost:8033/api/v1/consumption/logs",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ServiceUsageDTO>>() {}
            );

            List<ServiceUsageDTO> usageData = responseEntity.getBody();
            logger.info("Fetched {} service usage records", usageData != null ? usageData.size() : 0);
            return usageData != null ? usageData : List.of();
        } catch (Exception e) {
            logger.error("Error fetching service usage data: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Fetches revenue data synchronously.
     * @return List<RevenueDTO>
     */
    public List<RevenueDTO> fetchRevenueData() {
        try {
            logger.info("Fetching revenue data from /billing/all-invoices");

            ResponseEntity<List<RevenueDTO>> responseEntity = restTemplate.exchange(
                    "http://localhost:8084/billing/all-invoices",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RevenueDTO>>() {}
            );

            List<RevenueDTO> revenueData = responseEntity.getBody();
            logger.info("Fetched {} revenue records", revenueData != null ? revenueData.size() : 0);
            return revenueData != null ? revenueData : List.of();
        } catch (Exception e) {
            logger.error("Error fetching revenue data: {}", e.getMessage());
            return List.of();
        }
    }


    /**
     * Fetches the service name based on the access token synchronously.
     * @param accessToken The access token of the service.
     * @return String
     */
    public String fetchServiceNameByToken(String accessToken) {
        try {
            logger.info("Fetching service name for accessToken: {}", accessToken);
            String serviceName = restTemplate.getForObject(
                    "http://localhost:8081/service/api/services/service-name-by-token?accessToken={accessToken}",
                    String.class,
                    accessToken
            );
            logger.info("Fetched service name: {}", serviceName);
            return (serviceName != null && !serviceName.isEmpty()) ? serviceName : "Unknown Service";
        } catch (Exception e) {
            logger.warn("Failed to fetch service name for token {}: {}", accessToken, e.getMessage());
            return "Unknown Service";
        }
    }

    /**
     * Fetches the user's full name by their ID synchronously.
     * @param userId The ID of the user.
     * @return String representing the user's full name.
     */
    public String fetchUserNameById(String userId) {
        try {
            logger.info("Fetching user details for userId: {}", userId);
            UserDTO userDTO = restTemplate.getForObject("http://localhost:8884/admin/keycloak/user/" + userId, UserDTO.class);
            if (userDTO != null && userDTO.getFirstname() != null && userDTO.getLastName() != null) {
                String fullName = userDTO.getFirstname() + " " + userDTO.getLastName();
                logger.info("Fetched user name: {}", fullName);
                return fullName;
            } else {
                logger.warn("User details incomplete for userId: {}", userId);
                return "Unknown User";
            }
        } catch (Exception e) {
            logger.warn("User details missing for userId: {}: {}", userId, e.getMessage());
            return "Unknown User";
        }
    }

    /**
     * Fetches the group name by group ID synchronously.
     * @param groupId The ID of the group.
     * @return String representing the group's name.
     */
    public String fetchGroupNameById(Long groupId) {
        try {
            logger.info("Fetching group details for groupId: {}", groupId);
            UserGroupDTO groupDTO = restTemplate.getForObject("http://localhost:8884/api/v1/groups/details/" + groupId, UserGroupDTO.class);
            String groupName = (groupDTO != null && groupDTO.getName() != null) ? groupDTO.getName() : "Unknown Group";
            logger.info("Fetched group name: {}", groupName);
            return groupName;
        } catch (Exception e) {
            logger.warn("Failed to fetch group name for groupId: {}: {}", groupId, e.getMessage());
            return "Unknown Group";
        }
    }


}
