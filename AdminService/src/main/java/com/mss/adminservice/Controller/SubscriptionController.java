package com.mss.adminservice.Controller;

import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.SubscriptionRequest;
import com.mss.adminservice.Repo.SubscriptionRequestRepository;
import com.mss.adminservice.Service.GroupService;
import com.mss.adminservice.Service.NotificationService;
import com.mss.adminservice.Service.SubscriptionService;
import com.mss.adminservice.Config.ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@RestController
@RequestMapping("/admin/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionRequestRepository subscriptionRequestRepository;
    private final GroupService groupService;
    private final NotificationService notificationService;
    private final WebClient.Builder webClientBuilder;
    private final ServiceClient serviceClient;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService,
                                  SubscriptionRequestRepository subscriptionRequestRepository,
                                  GroupService groupService,
                                  NotificationService notificationService,
                                  WebClient.Builder webClientBuilder,
                                  ServiceClient serviceClient) {
        this.subscriptionService = subscriptionService;
        this.subscriptionRequestRepository = subscriptionRequestRepository;
        this.groupService = groupService;
        this.notificationService = notificationService;
        this.webClientBuilder = webClientBuilder;
        this.serviceClient = serviceClient;
    }

    @PostMapping("/request/{serviceId}")
    public ResponseEntity<String> requestSubscription(@RequestParam Long groupId, @RequestParam Long userId, @PathVariable UUID serviceId) {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setGroupId(groupId);
        subscriptionRequest.setUserId(userId);
        subscriptionRequest.setServiceId(serviceId);
        subscriptionRequest.setStatus("PENDING");
        subscriptionRequest.setNotificationStatus("PENDING");
        subscriptionRequestRepository.save(subscriptionRequest);
        notificationService.notifyAdminOfSubscriptionRequest(subscriptionRequest.getId(), userId, groupId);
        return ResponseEntity.ok("Subscription request submitted and admin notified successfully");
    }

    @PostMapping("/approve/{requestId}")
    public ResponseEntity<Map<String, String>> approveSubscription(@PathVariable Long requestId) {
        SubscriptionRequest subscriptionRequest = subscriptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Subscription request not found"));

        subscriptionRequest.setStatus("APPROVED");
        subscriptionRequest.setNotificationStatus("NOTIFIED");
        subscriptionRequestRepository.save(subscriptionRequest);

        // Get the service access token
        String serviceAccessToken = getServiceAccessToken(subscriptionRequest.getServiceId());
        if (serviceAccessToken == null) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to retrieve service access token"));
        }

        // Add the service access token to the group
        Group group = groupService.getGroupById(subscriptionRequest.getGroupId());
        if (group.getAccessTokens() == null) {
            group.setAccessTokens(new HashMap<>());
        }
        group.getAccessTokens().put(subscriptionRequest.getServiceId(), serviceAccessToken);
        group.setTokenGenerated(true);
        groupService.save(group);

        // Mark the notification as read and update its status
        notificationService.markNotificationAsReadByRequestId(requestId);

        return ResponseEntity.ok(Map.of("message", "Subscription request approved successfully"));
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Void> rejectSubscription(@PathVariable Long requestId) {
        SubscriptionRequest subscriptionRequest = subscriptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Subscription request not found"));

        subscriptionRequest.setStatus("REJECTED");
        subscriptionRequest.setNotificationStatus("NOTIFIED");
        subscriptionRequestRepository.save(subscriptionRequest);

        // Mark the notification as read and update its status
        notificationService.markNotificationAsReadByRequestId(requestId);

        return ResponseEntity.ok().build();
    }

    private String getServiceAccessToken(UUID serviceId) {
        // Make a call to the Service Manager to get the access token
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/service/api/services/getservice/" + serviceId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @GetMapping("/status/{serviceId}")
    public ResponseEntity<Map<String, String>> getSubscriptionStatus(@RequestParam Long groupId, @PathVariable UUID serviceId) {
        Optional<SubscriptionRequest> subscriptionRequest = subscriptionRequestRepository.findTopByGroupIdAndServiceIdOrderByIdDesc(groupId, serviceId);
        String status = subscriptionRequest.map(SubscriptionRequest::getStatus).orElse("NOT_SUBSCRIBED");
        Map<String, String> response = new HashMap<>();
        response.put("status", status);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/pending")
    public ResponseEntity<List<SubscriptionRequest>> getAllPendingRequests() {
        List<SubscriptionRequest> requests = subscriptionService.getAllPendingRequests();
        requests.forEach(request -> {
            String serviceName = subscriptionService.getServiceName(request.getServiceId());
            if (serviceName != null) {
                request.setServiceName(serviceName);
            } else {
                request.setServiceName("Unknown Service"); // Ensure non-null value
            }
        });
        return ResponseEntity.ok(requests);
    }


}
