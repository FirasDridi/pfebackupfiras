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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;

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

        // Extract Bearer token from Authorization header
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.error("Authorization header is missing or invalid.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be connected to access this service");
        }


        // Check if the user is authenticated
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authenticationToken == null || !authenticationToken.isAuthenticated()) {
            logger.error("User is not authenticated.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        // Extract the endpoint from the request URI
        String endpoint = request.getRequestURI();
        logger.info("Request URI: {}", endpoint);

        // Retrieve the access token of the service using the endpoint
        String serviceAccessToken = restClientService.getServiceAccessTokenByEndpoint(endpoint);
        logger.info("Service access token: {}", serviceAccessToken);

        if (serviceAccessToken == null || serviceAccessToken.isEmpty()) {
            logger.error("No access token found for the service.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No access token found for the service");
        }

        // Validate service access
        boolean isServiceAccessible = restClientService.isServiceAccessible(serviceAccessToken, endpoint);
        logger.info("Is service accessible: {}", isServiceAccessible);

        if (!isServiceAccessible) {
            logger.error("Access to the service denied.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access to the service denied");
        }

        // Extract Keycloak group name from the authentication token
        String keycloakGroupName = authenticationToken.getToken().getClaimAsString("group_id");
        logger.info("Extracted Keycloak Client name: {}", keycloakGroupName);

        if (keycloakGroupName == null) {
            logger.error("No Client associated with the user token");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No Client associated with the user token");
        }

        // Sanitize group name
        keycloakGroupName = keycloakGroupName.replace("/", "").replace("[", "").replace("]", "");
        logger.info("Sanitized Keycloak Client name: {}", keycloakGroupName);

        // Retrieve the database group ID using the sanitized Keycloak group name
        Long groupId = webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/admin/group-id-by-keycloak-name/" + keycloakGroupName)
                .retrieve()
                .bodyToMono(Long.class)
                .block();

        logger.info("Mapped Keycloak Client name '{}' to database Client ID {}", keycloakGroupName, groupId);

        if (groupId == null) {
            logger.error("No Client found in the database for Keycloak Client name: {}", keycloakGroupName);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No Client found in the database for the given Keycloak group name");
        }

        // Check if the group has access to the service
        boolean hasAccess = restClientService.isGroupServiceAccessible(groupId, serviceAccessToken);
        logger.info("Does Client ID {} have access to the service: {}", groupId, hasAccess);

        if (!hasAccess) {
            logger.error("Client ID {} does not have access to the service with access token: {}", groupId, serviceAccessToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client does not have access to the service");
        }

        // Log access details to Kafka
        Map<String, Object> accessDetails = new HashMap<>();
        accessDetails.put("accessToken", serviceAccessToken);
        accessDetails.put("endpoint", endpoint);
        accessDetails.put("timestamp", LocalDateTime.now());
        accessDetails.put("groupId", groupId);
        accessDetails.put("userId", authenticationToken.getToken().getClaimAsString("sub"));
        kafkaProducerService.sendAccessLog(accessDetails);

        logger.info("Service access granted for Client ID {} and user ID {}", groupId, accessDetails.get("userId"));
    }
}
