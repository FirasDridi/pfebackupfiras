package com.mss.servicemanager.Services;

import com.mss.servicemanager.DTO.SubscriptionRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SubscriptionService {

    private final WebClient webClient;

    public SubscriptionService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build(); // AdminService base URL
    }

    public void createSubscriptionRequest(SubscriptionRequestDTO subscriptionRequestDTO) {
        webClient.post()
                .uri("/admin/subscribe")
                .bodyValue(subscriptionRequestDTO)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
