// DataFetchingService.java
package com.example.statistics.service;

import com.example.statistics.dto.RevenueDTO;
import com.example.statistics.dto.ServiceUsageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class DataFetchingService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    public List<ServiceUsageDTO> fetchServiceUsageData() {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8033/api/v1/consumption/logs")
                .retrieve()
                .bodyToFlux(ServiceUsageDTO.class)
                .collectList()
                .block();
    }

    public List<RevenueDTO> fetchRevenueData() {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8084/billing/all-invoices")
                .retrieve()
                .bodyToFlux(RevenueDTO.class)
                .collectList()
                .block();
    }

    public String fetchServiceNameByToken(String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/service/api/services/service-name-by-token?accessToken=" + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
