package com.mss.servicemanager.Controller;

import com.mss.base.controller.*;
import com.mss.base.controller.impl.BaseControlerImpl;
import com.mss.servicemanager.DTO.*;
import com.mss.servicemanager.Repositories.ServiceUsageRepo;
import com.mss.servicemanager.Services.ServiceUsageService;
import com.mss.servicemanager.entities.service;
import org.example.annotations.Payant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/services")
public class ServiceUsageController extends BaseControlerImpl<service, UUID, ServiceDto, ServiceDto>
        implements
        FetchByIdController<service, UUID, ServiceDto, ServiceDto>,
        SaveController<service, UUID, ServiceDto, ServiceDto>,
        FindAllController<service, UUID, ServiceDto, ServiceDto>,
        DeleteController<service, UUID, ServiceDto, ServiceDto>,
        UpdateController<service, UUID, ServiceDto, ServiceDto>,
        AdvancedSearchController<service, UUID, ServiceDto, ServiceDto> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceUsageController.class);

    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private ServiceUsageRepo serviceRepository;
    @Autowired
    private ServiceUsageService serviceUsageService;

    @PatchMapping("/{serviceUsageId}/activate")
    public ResponseEntity<String> activateService(@PathVariable UUID serviceUsageId) {
        Optional<service> serviceUsageOptional = serviceUsageService.findById(serviceUsageId);
        if (serviceUsageOptional.isPresent()) {
            service serviceUsage = serviceUsageOptional.get();
            serviceUsage.setStatus(true);
            serviceUsageService.save(serviceUsage);
            return ResponseEntity.ok().body("{\"message\": \"Service activated successfully\"}");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{serviceUsageId}/deactivate")
    public ResponseEntity<String> deactivateService(@PathVariable UUID serviceUsageId) {
        Optional<service> serviceUsageOptional = serviceUsageService.findById(serviceUsageId);
        if (serviceUsageOptional.isPresent()) {
            service serviceUsage = serviceUsageOptional.get();
            serviceUsage.setStatus(false);
            serviceUsageService.save(serviceUsage);
            return ResponseEntity.ok().body("{\"message\": \"Service deactivated successfully\"}");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/listWithDetails")
    public Mono<ResponseEntity<List<ServiceDetailsDto>>> listServicesWithDetails() {
        return serviceUsageService.findAllServicesWithDetails()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/group-services")
    public ResponseEntity<List<ServiceDto>> getServicesForUserGroups(@PathVariable Long userId) {
        List<ServiceDto> services = serviceUsageService.getServicesForUserGroups(userId);
        return ResponseEntity.ok(services);
    }

    private List<GroupDto> fetchUserGroups(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/admin/user/{userId}/groups", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GroupDto>>() {})
                .block();
    }

    @Payant
    @PostMapping("/useService")
    public ResponseEntity<String> useService(@RequestHeader("Authorization") String accessToken) {
        boolean isSuccess = serviceUsageService.useService(accessToken);
        if (isSuccess) {
            return ResponseEntity.ok("Service used successfully");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access to the service denied");
        }
    }

    @GetMapping("/access-token/{accessToken}")
    public ResponseEntity<ServiceDto> getServiceByAccessToken(@PathVariable String accessToken) {
        Optional<service> serviceOptional = serviceRepository.findByAccessToken(accessToken);

        if (serviceOptional.isPresent()) {
            service service = serviceOptional.get();
            logger.info("Service: {}", service);

            // Building ServiceDto
            ServiceDto serviceDTO = new ServiceDto();
            serviceDTO.setId(service.getId());
            serviceDTO.setAccessToken(service.getAccessToken());
            serviceDTO.setConfiguration(service.getConfiguration());
            serviceDTO.setCreatedDate(service.getCreatedDate());
            serviceDTO.setDescription(service.getDescription());
            serviceDTO.setEndpoint(service.getEndpoint());
            serviceDTO.setLastModifiedDate(service.getLastModifiedDate());
            serviceDTO.setName(service.getName());
            serviceDTO.setPricing(service.getPricing());
            serviceDTO.setStatus(service.isStatus());
            serviceDTO.setVersion(service.getVersion());

            return ResponseEntity.ok(serviceDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateServiceAccess(@RequestParam String token, @RequestParam String endpoint) {
        Optional<service> serviceOptional = serviceRepository.findByEndpoint(endpoint);
        if (serviceOptional.isPresent()) {
            service foundService = serviceOptional.get();
            if (foundService.getAccessToken().equals(token)) {
                return ResponseEntity.ok(true);
            } else {
                logger.error("Access token mismatch for endpoint {}: expected {}, but got {}", endpoint, foundService.getAccessToken(), token);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
            }
        } else {
            logger.error("Service not found for endpoint {}", endpoint);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }
    }


    @PatchMapping("/{serviceId}/add-access-token")
    public ResponseEntity<String> addAccessTokenToService(@PathVariable UUID serviceId) {
        service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found: " + serviceId));
        String accessToken = UUID.randomUUID().toString();
        service.setAccessToken(accessToken);
        serviceRepository.save(service);
        return ResponseEntity.ok("Access token generated and added successfully to the service");
    }

    @GetMapping("/getservice/{serviceId}")
    public ResponseEntity<String> getAccesToken(@PathVariable UUID serviceId) {
        service serv = serviceRepository.findById(serviceId).orElse(null);
        if (serv == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
        }
        String at = serv.getAccessToken();
        return ResponseEntity.ok(at);
    }
    @Payant
        @PostMapping("/firastest")
    public ResponseEntity<String> ttc() {
        return ResponseEntity.ok("firas");
    }

    @Payant
    @PostMapping("/firastestt")
    public ResponseEntity<String> tttc() {
        return ResponseEntity.ok("firas");
    }
    @Payant
    @PostMapping("/firastesttttttt")
    public ResponseEntity<String> ttyytc() {
        return ResponseEntity.ok("firas");
    }
    @Payant
    @PostMapping("/hamza")
    public ResponseEntity<String> ttykytc() {
        return ResponseEntity.ok("firas");
    }
    @GetMapping("/access-token")
    public ResponseEntity<String> getServiceAccessTokenByEndpoint(@RequestParam String endpoint) {
        Optional<service> serviceOptional = serviceRepository.findByEndpoint(endpoint);
        if (serviceOptional.isPresent()) {
            return ResponseEntity.ok(serviceOptional.get().getAccessToken());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @GetMapping("/getservicename/{serviceId}")
    public ResponseEntity<String> getServiceName(@PathVariable UUID serviceId) {
        String serviceName = serviceUsageService.getServiceName(serviceId);
        if (serviceName != null) {
            return ResponseEntity.ok(serviceName);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/{serviceId}/invoices")
    public ResponseEntity<List<InvoiceDetailsDto>> getInvoicesByServiceId(@PathVariable UUID serviceId) {
        List<InvoiceDetailsDto> invoiceDetails = fetchInvoices(serviceId);

        if (invoiceDetails == null || invoiceDetails.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Add logic to fetch userName and groupName if required
        invoiceDetails.forEach(invoice -> {
            invoice.setUserName(fetchUserName(invoice.getUserId()));
            invoice.setGroupName(fetchGroupName(invoice.getGroupId()));
        });

        return ResponseEntity.ok(invoiceDetails);
    }

    private List<InvoiceDetailsDto> fetchInvoices(UUID serviceId) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8084/billing/service/" + serviceId + "/invoices")
                .retrieve()
                .bodyToFlux(InvoiceDetailsDto.class)
                .collectList()
                .block();
    }

    private String fetchUserName(   UUID userId) {
        UserDto userDto = webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/admin/getUser/" + userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();

        // Log the userDto to ensure it's correctly populated
        System.out.println(userDto);

        return userDto != null ? userDto.getUsername() : null;
    }




    private String fetchGroupName(Long groupId) {
        GroupDto group = webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/api/v1/groups/details/" + groupId)
                .retrieve()
                .bodyToMono(GroupDto.class)
                .block();
        return group != null ? group.getName() : null;
    }
    @GetMapping("/service-name-by-token")
    public ResponseEntity<String> getServiceNameByAccessToken(@RequestParam String accessToken) {
        Optional<service> serviceOptional = serviceRepository.findByAccessToken(accessToken);
        if (serviceOptional.isPresent()) {
            String serviceName = serviceOptional.get().getName();
            return ResponseEntity.ok(serviceName);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found for the given access token");
        }
    }

}


