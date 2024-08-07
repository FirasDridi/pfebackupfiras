package com.mss.adminservice.Service;

import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.ServiceGroupMapping;
import com.mss.adminservice.Repo.GroupRepository;
import com.mss.adminservice.Repo.ServiceGroupMappingRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ServiceGroupMappingService {

    @Autowired
    private final ServiceGroupMappingRepository repository;
    private final GroupRepository groupRepository;

    public ServiceGroupMapping addGroupToService(UUID serviceId, Long groupId) {
        if (serviceId == null) {
            throw new IllegalArgumentException("Service ID cannot be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("Group ID cannot be null");
        }

        Optional<Group> group = groupRepository.findById(groupId);
        if (group.isEmpty()) {
            throw new IllegalArgumentException("Group not found");
        }

        ServiceGroupMapping mapping = new ServiceGroupMapping();
        mapping.setServiceId(serviceId.toString()); // Correct field name
        mapping.setGroup(group.get()); // Correct field name

        return repository.save(mapping);
    }

    @Transactional
    public void removeGroupFromService(UUID serviceId, Long groupId) {
        repository.deleteByServiceNameAndGroupId(serviceId.toString(), groupId); // Ensure this method exists in the repository
    }

    public boolean hasAccessToService(UUID serviceId, String groupId) {
        return repository.existsByServiceNameAndGroupId(serviceId.toString(), Long.valueOf(groupId)); // Ensure this method exists in the repository
    }
}
