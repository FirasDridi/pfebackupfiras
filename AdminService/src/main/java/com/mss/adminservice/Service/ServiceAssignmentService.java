package com.mss.adminservice.Service;

import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.ServiceGroupMapping;
import com.mss.adminservice.Entities.User;
import com.mss.adminservice.Repo.GroupRepository;
import com.mss.adminservice.Repo.ServiceGroupMappingRepository;
import com.mss.adminservice.Repo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class ServiceAssignmentService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ServiceGroupMappingRepository serviceGroupMappingRepository;

    @Transactional
    public void assignServiceToGroup(String serviceName, Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        ServiceGroupMapping mapping = new ServiceGroupMapping();
        mapping.setServiceName(serviceName);
        mapping.setGroup(group);
        serviceGroupMappingRepository.save(mapping);

        // Update users in the group
        updateUsersServicesInGroup(group);
    }

    @Transactional
    public void removeServiceFromGroup(String serviceName, Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        serviceGroupMappingRepository.deleteByGroupAndServiceName(group, serviceName);

        // Update users in the group
        updateUsersServicesInGroup(group);
    }

    public Set<String> getServicesForUser(User user) {
        Set<String> userServices = new HashSet<>();
        for (Group group : user.getGroups()) {
            List<ServiceGroupMapping> mappings = serviceGroupMappingRepository.findByGroup(group);
            for (ServiceGroupMapping mapping : mappings) {
                userServices.add(mapping.getServiceName());
            }
        }
        return userServices;
    }

    private void updateUsersServicesInGroup(Group group) {
        List<ServiceGroupMapping> mappings = serviceGroupMappingRepository.findByGroup(group);
        Set<String> services = new HashSet<>();
        for (ServiceGroupMapping mapping : mappings) {
            services.add(mapping.getServiceName());
        }
        for (User user : group.getUsers()) {
            user.getServices().clear();
            user.getServices().addAll(services);
            userRepository.save(user);
        }
    }
}
