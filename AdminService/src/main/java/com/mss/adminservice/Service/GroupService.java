package com.mss.adminservice.Service;

import com.mss.adminservice.Config.KeycloakConfig;
import com.mss.adminservice.Config.UserGroupDTO;
import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.ServiceGroupMapping;
import com.mss.adminservice.Entities.User;
import com.mss.adminservice.Repo.GroupRepository;
import com.mss.adminservice.Repo.ServiceGroupMappingRepository;
import com.mss.adminservice.Repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final KeycloakConfig keycloakConfig;
    private final UserRepository userRepository;
    private final ServiceGroupMappingRepository serviceGroupMappingRepository;
    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    @Transactional
    public Group addGroup(Group group) {
        Group savedGroup = groupRepository.save(group);

        GroupRepresentation groupRep = new GroupRepresentation();
        groupRep.setName(group.getName());

        GroupsResource groupsResource = keycloakConfig.getInstance()
                .realm(KeycloakConfig.realm)
                .groups();
        groupsResource.add(groupRep);

        return savedGroup;
    }

    @Transactional
    public Group updateGroup(Long groupId, Group groupDetails) {
        Group group = getGroupById(groupId);
        group.setName(groupDetails.getName());
        Group updatedGroup = groupRepository.save(group);

        updateGroupInKeycloak(group);

        return updatedGroup;
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = getGroupById(groupId);
        groupRepository.delete(group);

        deleteGroupInKeycloak(group);
    }

    public List<User> getUsersByGroup(Long groupId) {
        return userRepository.findByGroupsId(groupId);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupById(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
    }

    public Optional<Group> getGroupByName(String groupName) {
        return groupRepository.findByName(groupName);
    }

    public List<Group> searchGroupsByName(String groupName) {
        return groupRepository.findByNameContaining(groupName);
    }

    @Transactional
    public void deleteAllGroups() {
        groupRepository.deleteAll();
        if (groupRepository.count() == 0) {
            deleteAllGroupsInKeycloak();
        }
    }

    private void updateGroupInKeycloak(Group group) {
        GroupsResource groupsResource = keycloakConfig.getInstance().realm(KeycloakConfig.realm).groups();
        List<GroupRepresentation> groups = groupsResource.groups();
        for (GroupRepresentation groupRep : groups) {
            if (groupRep.getName().equals(group.getName())) {
                groupRep.setName(group.getName());
                groupsResource.group(groupRep.getId()).update(groupRep);
                return;
            }
        }
    }

    private void deleteGroupInKeycloak(Group group) {
        GroupsResource groupsResource = keycloakConfig.getInstance().realm(KeycloakConfig.realm).groups();
        List<GroupRepresentation> groups = groupsResource.groups();
        for (GroupRepresentation groupRep : groups) {
            if (groupRep.getName().equals(group.getName())) {
                groupsResource.group(groupRep.getId()).remove();
                return;
            }
        }
    }

    private void deleteAllGroupsInKeycloak() {
        GroupsResource groupsResource = keycloakConfig.getInstance().realm(KeycloakConfig.realm).groups();
        List<GroupRepresentation> groups = groupsResource.groups();
        for (GroupRepresentation groupRep : groups) {
            groupsResource.group(groupRep.getId()).remove();
        }
    }

    public Group addUserToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        group.getUsers().add(user);
        user.getGroups().add(group);

        groupRepository.save(group);
        userRepository.save(user);

        return group;
    }

    @Transactional
    public Group toggleToken(Long id, UUID serviceId) {
        Optional<Group> groupOptional = groupRepository.findById(id);
        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();
            Map<UUID, String> accessTokens = group.getAccessTokens();
            if (accessTokens.containsKey(serviceId)) {
                accessTokens.remove(serviceId);
            } else {
                accessTokens.put(serviceId, UUID.randomUUID().toString());
            }
            group.setTokenGenerated(!accessTokens.isEmpty());
            groupRepository.save(group);
            return group;
        }
        return null;
    }

    public String generateTokenForService(Group group, UUID serviceId) {
        return UUID.randomUUID().toString();
    }

    public List<Object> getGroupsForService(String serviceId) {
        List<ServiceGroupMapping> mappings = serviceGroupMappingRepository.findByServiceId(serviceId);
        logger.info("Fetched mappings for serviceId {}: {}", serviceId, mappings);
        return mappings.stream().map(ServiceGroupMapping::getGroup).collect(Collectors.toList());
    }

    public List<UserGroupDTO> getUserGroupsByUserId(String userId) {
        Optional<User> user = userRepository.findByKeycloakId(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        List<Group> groups = groupRepository.findByUsersContaining(Optional.of(user.get()));
        return groups.stream().map(group -> {
            UserGroupDTO userGroupDTO = new UserGroupDTO();
            userGroupDTO.setGroupId(group.getId());
            userGroupDTO.setGroupName(group.getName());
            userGroupDTO.setDescription(group.getDescription());
            userGroupDTO.setId(group.getId());
            System.out.println(group.getId());
            return userGroupDTO;
        }).collect(Collectors.toList());
    }

    public List<Group> getGroupsByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return new ArrayList<>(user.getGroups());
    }

    public void save(Group group) {
        groupRepository.save(group);
    }
}
