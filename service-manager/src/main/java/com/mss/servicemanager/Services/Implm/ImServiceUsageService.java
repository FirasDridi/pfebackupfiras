package com.mss.servicemanager.Services.Implm;

import com.mss.base.repositories.BaseRepository;
import com.mss.base.service.impl.AbstractBaseServiceImpl;
import com.mss.servicemanager.DTO.GroupDto;
import com.mss.servicemanager.DTO.ServiceDetailsDto;
import com.mss.servicemanager.DTO.ServiceDto;
import com.mss.servicemanager.DTO.UserDto;
import com.mss.servicemanager.Repositories.ServiceUsageRepo;
import com.mss.servicemanager.Services.ServiceUsageService;
import com.mss.servicemanager.entities.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImServiceUsageService extends AbstractBaseServiceImpl<service, UUID> implements ServiceUsageService {

    @Autowired
    private ServiceUsageRepo serviceUsageRepo;

    @Autowired
    private WebClient.Builder webClientBuilder;

    protected ImServiceUsageService(BaseRepository<service, UUID> repository) {
        super(repository);
    }

    @Override
    public Mono<List<ServiceDetailsDto>> findAllServicesWithDetails() {
        List<service> services = serviceUsageRepo.findAll();
        return Mono.just(services.stream().map(service -> {
            ServiceDetailsDto dto = new ServiceDetailsDto();
            dto.setId(UUID.fromString(service.getId().toString()));
            dto.setName(service.getName());
            dto.setDescription(service.getDescription());
            dto.setVersion(service.getVersion());
            dto.setEndpoint(service.getEndpoint());
            dto.setStatus(service.isStatus());
            dto.setConfiguration(service.getConfiguration());
            dto.setPricing(service.getPricing());

            // Fetch groups for this service
            List<GroupDto> groupDtos = fetchGroupsForService(service.getId().toString());
            dto.setGroups(groupDtos);

            // Fetch users for each group and aggregate them
            List<UserDto> userDtos = groupDtos.stream()
                    .flatMap(group -> fetchUsersForGroup(group.getId()).stream())
                    .collect(Collectors.toList());
            dto.setUsers(userDtos);

            return dto;
        }).collect(Collectors.toList()));
    }

    @Override
    public List<ServiceDto> getServicesForGroup(Long groupId) {
        return null;
    }

    @Override
    public void addGroupToService(String serviceId, Long groupId) {

    }

    @Override
    public void removeGroupFromService(String serviceId, Long groupId) {

    }

    @Override
    public boolean hasAccessToService(String serviceId, Long groupId) {
        return false;
    }

    private List<GroupDto> fetchGroupsForService(String serviceId) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/admin/groupsForService/{serviceId}", serviceId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GroupDto>>() {})
                .block();
    }

    private List<UserDto> fetchUsersForGroup(Long groupId) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8884/admin/groups/{groupId}/users", groupId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserDto>>() {})
                .block();
    }

    @Override
    public boolean useService(String accessToken) {
        // Logic to validate the access token, fetch the group ID, and check if the user belongs to the group.
        // Implement the logic here similar to the previous provided implementation.
        return true; // Or false based on validation results.
    }
    @Override
    public String getServiceName(UUID serviceId) {
        return serviceUsageRepo.findById(serviceId)
                .map(service::getName)
                .orElse(null);
    }
}
