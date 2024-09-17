package org.example.aspects;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.services.RestClientService;
import org.example.services.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class PayantAspect {

    @Autowired
    private RestClientService restClientService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final Logger logger = LoggerFactory.getLogger(PayantAspect.class);

    @Before("@annotation(org.example.annotations.Payant)")
    public void checkServiceAccess(JoinPoint joinPoint) throws Throwable {
        logger.info("Starting service access check.");

        // Extract the request attributes
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // Extract the endpoint from the request URI
        String endpoint = request.getRequestURI();
        logger.info("Request URI: {}", endpoint);

        // Retrieve the access token of the service using the endpoint
        String serviceAccessToken = restClientService.getServiceAccessTokenByEndpoint(endpoint);
        logger.info("Service access token: {}", serviceAccessToken);

        if (serviceAccessToken == null || serviceAccessToken.isEmpty()) {
            logger.error("No access token found for the service.");
            throw new SecurityException("No access token found for the service");
        }

        // Validate service access
        boolean isServiceAccessible = restClientService.isServiceAccessible(serviceAccessToken, endpoint);
        logger.info("Is service accessible: {}", isServiceAccessible);

        if (!isServiceAccessible) {
            logger.error("Access to the service denied.");
            throw new SecurityException("Access to the service denied");
        }

        // Retrieve the user authentication token
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        // Extract Keycloak group name from the authentication token
        String keycloakGroupName = authenticationToken.getToken().getClaimAsString("group_id"); // This should be the group name in Keycloak
        logger.info("Extracted Keycloak group name: {}", keycloakGroupName);

        if (keycloakGroupName == null) {
            logger.error("No group associated with the user token");
            throw new SecurityException("No group associated with the user token");
        }

        // Remove any '/' characters and square brackets '[]' from the group name
        keycloakGroupName = keycloakGroupName.replace("/", "").replace("[", "").replace("]", "");
        logger.info("Sanitized Keycloak group name: {}", keycloakGroupName);

        // Retrieve the database group ID using the sanitized Keycloak group name
        Long groupId = webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/admin/group-id-by-keycloak-name/" + keycloakGroupName)
                .retrieve()
                .bodyToMono(Long.class)
                .block();

        logger.info("Mapped Keycloak group name '{}' to database group ID {}", keycloakGroupName, groupId);

        if (groupId == null) {
            logger.error("No group found in the database for Keycloak group name: {}", keycloakGroupName);
            throw new SecurityException("No group found in the database for the given Keycloak group name");
        }

        // Check if the group has access to the service
        boolean hasAccess = restClientService.isGroupServiceAccessible(groupId, serviceAccessToken);
        logger.info("Does group ID {} have access to the service: {}", groupId, hasAccess);

        if (!hasAccess) {
            logger.error("Group ID {} does not have access to the service with access token: {}", groupId, serviceAccessToken);
            throw new SecurityException("Group does not have access to the service");
        }

        // Log access details to Kafka
        Map<String, Object> accessDetails = new HashMap<>();
        accessDetails.put("accessToken", serviceAccessToken);
        accessDetails.put("endpoint", endpoint);
        accessDetails.put("timestamp", LocalDateTime.now());
        accessDetails.put("groupId", groupId);
        accessDetails.put("userId", authenticationToken.getToken().getClaimAsString("sub"));
        kafkaProducerService.sendAccessLog(accessDetails);

        logger.info("Service access granted for group ID {} and user ID {}", groupId, accessDetails.get("userId"));
    }
}
