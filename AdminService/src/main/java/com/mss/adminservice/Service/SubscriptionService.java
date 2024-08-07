package com.mss.adminservice.Service;

import com.mss.adminservice.Entities.SubscriptionRequest;
import com.mss.adminservice.Repo.GroupRepository;
import com.mss.adminservice.Repo.SubscriptionRequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class SubscriptionService {

    private final SubscriptionRequestRepository subscriptionRequestRepository;
    private final GroupRepository groupRepository;
    private final WebClient.Builder webClientBuilder;

    public SubscriptionRequest createSubscriptionRequest(Long groupId, UUID serviceId) {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setGroupId(groupId);
        request.setServiceId(serviceId);
        request.setStatus("PENDING");
        return subscriptionRequestRepository.save(request);
    }

    public void approveRequest(Long requestId) {
        SubscriptionRequest request = subscriptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Subscription request not found: " + requestId));
        request.setStatus("APPROVED");

        // Update the group's access token
        groupRepository.findById(request.getGroupId()).ifPresent(group -> {
            group.getAccessTokens().put(request.getServiceId(), generateAccessToken());
            group.setTokenGenerated(true);
            groupRepository.save(group);
        });

        subscriptionRequestRepository.save(request);
    }

    public void rejectRequest(Long requestId) {
        SubscriptionRequest request = subscriptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Subscription request not found: " + requestId));
        request.setStatus("REJECTED");
        subscriptionRequestRepository.save(request);
    }

    public boolean isServiceAccessibleByGroup(UUID serviceId, Long groupId) {
        return subscriptionRequestRepository.findByStatus("APPROVED").stream()
                .anyMatch(request -> request.getGroupId().equals(groupId) && request.getServiceId().equals(serviceId));
    }

    public List<SubscriptionRequest> getAllPendingRequests() {
        return subscriptionRequestRepository.findByStatus("PENDING");
    }

    private String generateAccessToken() {
        // Logic to generate a secure access token
        return UUID.randomUUID().toString();
    }

    public String getServiceName(UUID serviceId) {
        // Make a call to the Service Manager to get the service name
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/service/api/services/getservicename/" + serviceId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
