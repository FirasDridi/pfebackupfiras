package com.mss.servicemanager.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final WebClient userServiceWebClient;
    private final WebClient adminServiceWebClient;

    @Autowired
    public UserService(WebClient userServiceWebClient, WebClient adminServiceWebClient) {
        this.userServiceWebClient = userServiceWebClient;
        this.adminServiceWebClient = adminServiceWebClient;
    }

    public Mono<String> getUserById(String userId) {
        return userServiceWebClient.get()
                .uri("/{id}", userId)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getAllUsers() {
        return adminServiceWebClient.get()
                .uri("/allUsers")
                .retrieve()
                .bodyToMono(String.class);
    }
}
