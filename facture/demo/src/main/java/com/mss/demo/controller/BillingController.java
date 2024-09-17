package com.mss.demo.controller;

import com.mss.demo.entity.Invoice;
import com.mss.demo.entity.dto.AccessLogDTO;
import com.mss.demo.entity.dto.InvoiceDetailDTO;
import com.mss.demo.repository.InvoiceRepository;
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
    @Autowired
    private InvoiceRepository invoiceRepository;
    @GetMapping("/user/{userId}/invoices")
        public List<InvoiceDetailDTO> getUserInvoices(@PathVariable String userId) {
        return billingService.getUserInvoices(userId);
    }

    @GetMapping("/group/{groupId}/invoices")
    public List<InvoiceDetailDTO> getGroupInvoices(@PathVariable Long groupId) {
        return billingService.getGroupInvoices(groupId);
    }

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
    public ResponseEntity<Double> getTotalAmountForGroup(@PathVariable Long groupId) {
        double totalAmount = billingService.getTotalAmountForGroup(groupId);
        return ResponseEntity.ok(totalAmount);
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

    @PostMapping("/generate/user")
    public ResponseEntity<String> generateUserInvoices() {
        try {
            List<AccessLogDTO> accessLogs = billingService.fetchAccessLogs();
            billingService.generateUserInvoices(accessLogs);
            return ResponseEntity.ok("User invoices generated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(503).body(e.getMessage());
        }
    }

    @PostMapping("/generate/group")
    public ResponseEntity<String> generateGroupInvoices() {
        try {
            List<AccessLogDTO> accessLogs = billingService.fetchAccessLogs();
            billingService.generateGroupInvoices(accessLogs);
            return ResponseEntity.ok("Group invoices generated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(503).body(e.getMessage());
        }
    }
    @GetMapping("/service/{serviceId}/invoices")
    public ResponseEntity<List<Invoice>> getInvoicesByServiceId(@PathVariable UUID serviceId) {
        List<Invoice> invoices = invoiceRepository.findByServiceId(serviceId);
        if (invoices.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(invoices);
    }
}
