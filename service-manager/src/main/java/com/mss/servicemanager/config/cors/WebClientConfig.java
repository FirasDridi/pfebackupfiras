package com.mss.servicemanager.config.cors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${user-service.base-url}")
    private String userServiceBaseUrl;

    @Value("${service-manager-service.base-url}")
    private String serviceManagerServiceBaseUrl;

    @Value("${admin-service.base-url}")
    private String adminServiceBaseUrl;

    @Bean
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl(userServiceBaseUrl)
                .build();
    }

    @Bean
    public WebClient serviceManagerWebClient() {
        return WebClient.builder()
                .baseUrl(serviceManagerServiceBaseUrl)
                .build();
    }

    @Bean
    public WebClient adminServiceWebClient() {
        return WebClient.builder()
                .baseUrl(adminServiceBaseUrl)
                .build();
    }
}
