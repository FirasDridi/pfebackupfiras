package com.example.kafkaconsumer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ExternalServiceClient {

    private final WebClient serviceManagerWebClient;
    private final WebClient adminServiceWebClient;

    @Autowired
    public ExternalServiceClient(
            @Qualifier("serviceManagerWebClient") WebClient serviceManagerWebClient,
            @Qualifier("adminServiceWebClient") WebClient adminServiceWebClient) {
        this.serviceManagerWebClient = serviceManagerWebClient;
        this.adminServiceWebClient = adminServiceWebClient;
    }

    public Mono<String> callServiceManager(UUID serviceId) {
        return serviceManagerWebClient
                .get()
                .uri("/api/services/" + serviceId)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> callAdminService(Long userId) {
        return adminServiceWebClient
                .get()
                .uri("/admin/user/" + userId + "/groups")
                .retrieve()
                .bodyToMono(String.class);
    }
}
