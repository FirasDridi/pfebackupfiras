package com.mss.demo.controller;

import com.mss.demo.entity.dto.InvoiceDetailDTO;
import com.mss.demo.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @GetMapping("/user/{userId}/invoices")
    public List<InvoiceDetailDTO> getUserInvoices(@PathVariable Long userId) {
        return billingService.getUserInvoices(String.valueOf(userId));
    }

    @GetMapping("/group/{groupId}/invoices")
    public List<InvoiceDetailDTO> getGroupInvoices(@PathVariable Long groupId) {
        return billingService.getGroupInvoices(groupId);
    }

    // BillingController.java

    @GetMapping("/user/{userId}/total")
    public ResponseEntity<Double> getTotalAmountForUser(@PathVariable String userId) {
        try {
            UUID userUUID = UUID.fromString(userId);
            double totalAmount = billingService.getTotalAmountForUser(userUUID);
            return ResponseEntity.ok(totalAmount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(0.0);
        }
    }




    @GetMapping("/group/{groupId}/total")
    public double getTotalAmountForGroup(@PathVariable Long groupId) {
        return billingService.getTotalAmountForGroup(groupId);
    }

    @GetMapping("/all-invoices")
    public List<InvoiceDetailDTO> getAllInvoices() {
        return billingService.getAllInvoices().stream()
                .map(billingService::toInvoiceDetailDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateInvoices() {
        try {
            billingService.generateInvoices();
            return ResponseEntity.ok("Invoices generated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(503).body(e.getMessage());
        }
    }
}
