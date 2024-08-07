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

    public Long getGroupIdFromToken(String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/admin/group-id-by-token/" + accessToken)
                .retrieve()
                .bodyToMono(Long.class)
                .block();
    }

    public Long getUserIdFromToken(String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/admin/user-id-by-token/" + accessToken)
                .retrieve()
                .bodyToMono(Long.class)
                .block();
    }
}
