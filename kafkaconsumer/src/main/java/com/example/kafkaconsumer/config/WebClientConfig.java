package com.example.kafkaconsumer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${service.manager.url}")
    private String serviceManagerUrl;

    @Value("${admin.service.url}")
    private String adminServiceUrl;

    @Bean
    public WebClient serviceManagerWebClient() {
        return WebClient.builder()
                .baseUrl(serviceManagerUrl)
                .build();
    }

    @Bean
    public WebClient adminServiceWebClient() {
        return WebClient.builder()
                .baseUrl(adminServiceUrl)
                .build();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
