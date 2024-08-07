package com.mss.servicemanager.Services;

import com.mss.base.service.BaseService;
import com.mss.servicemanager.DTO.ServiceDetailsDto;
import com.mss.servicemanager.DTO.ServiceDto;
import com.mss.servicemanager.entities.service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ServiceUsageService extends BaseService<service, UUID> {
    Mono<List<ServiceDetailsDto>> findAllServicesWithDetails();

    default List<ServiceDto> getServicesForUserGroups(Long userId) {
        return null;
    }

    List<ServiceDto> getServicesForGroup(Long groupId);

    void addGroupToService(String serviceId, Long groupId);

    void removeGroupFromService(String serviceId, Long groupId);

    boolean hasAccessToService(String serviceId, Long groupId);

    boolean useService(String accessToken);
    public String getServiceName(UUID serviceId) ;
}
