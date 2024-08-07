package org.example.aspects;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.services.RestClientService;
import org.example.services.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    @Before("@annotation(org.example.annotations.Payant)")
    public void checkServiceAccess(JoinPoint joinPoint) throws Throwable {
        // Extract the request attributes
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // Extract the endpoint from the request URI
        String endpoint = request.getRequestURI();

        // Retrieve the access token of the service using the endpoint
        String serviceAccessToken = restClientService.getServiceAccessTokenByEndpoint(endpoint);

        if (serviceAccessToken == null || serviceAccessToken.isEmpty()) {
            throw new SecurityException("No access token found for the service");
        }

        // Validate service access
        boolean isServiceAccessible = restClientService.isServiceAccessible(serviceAccessToken, endpoint);
        if (!isServiceAccessible) {
            throw new SecurityException("Access to the service denied");
        }

        // Retrieve the group ID using the access token
        Long groupId = restClientService.getGroupIdFromToken(serviceAccessToken);

        if (groupId == null) {
            throw new SecurityException("No client associated with the access token");
        }

        // Retrieve the user ID from the authentication token
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String userId = authenticationToken.getToken().getClaimAsString("sub");

        if (userId == null) {
            throw new SecurityException("No user associated with the access token");
        }

        // Log access details to Kafka
        Map<String, Object> accessDetails = new HashMap<>();
        accessDetails.put("accessToken", serviceAccessToken);
        accessDetails.put("endpoint", endpoint);
        accessDetails.put("timestamp", LocalDateTime.now());
        accessDetails.put("groupId", groupId);
        accessDetails.put("userId", userId);
        kafkaProducerService.sendAccessLog(accessDetails);
    }
}
