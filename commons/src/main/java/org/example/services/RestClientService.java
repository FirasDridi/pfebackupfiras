package org.example.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RestClientService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    public boolean isServiceAccessible(String accessToken, String endpoint) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/service/api/services/validate?token=" + accessToken + "&endpoint=" + endpoint)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    public String getServiceAccessTokenByEndpoint(String endpoint) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/service/api/services/access-token?endpoint=" + endpoint)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public Long getGroupIdByKeycloakName(String keycloakGroupName) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/admin/group-id-by-keycloak-name/" + keycloakGroupName) // Adjusted URI
                .retrieve()
                .bodyToMono(Long.class)
                .block();
    }

    public boolean isGroupServiceAccessible(Long groupId, String serviceAccessToken) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/api/v1/groups/" + groupId + "/service-access/" + serviceAccessToken)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
