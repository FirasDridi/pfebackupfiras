package com.mss.adminservice.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class ServiceClient {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private String getServiceAccessToken(UUID serviceId) {
        // Make a call to the Service Manager to get the access token
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/service/api/services/getservice/" + serviceId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
