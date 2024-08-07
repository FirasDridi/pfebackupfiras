package com.mss.servicemanager.Controller;

import com.mss.servicemanager.DTO.SubscriptionRequestDTO;
import com.mss.servicemanager.Services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping("/request")
    public ResponseEntity<Void> createSubscriptionRequest(@RequestBody SubscriptionRequestDTO subscriptionRequestDTO) {
        subscriptionService.createSubscriptionRequest(subscriptionRequestDTO);
        return ResponseEntity.ok().build();
    }
}
