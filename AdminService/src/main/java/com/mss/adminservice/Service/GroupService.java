package com.mss.adminservice.Service;

import com.mss.adminservice.Config.KeycloakConfig;
import com.mss.adminservice.Config.UserGroupDTO;
import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.ServiceGroupMapping;
import com.mss.adminservice.Entities.SubscriptionRequest;
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

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final KeycloakConfig keycloakConfig;
    private final KeyCloakService keycloakService; // Injected KeyCloakService

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
        Response response = groupsResource.add(groupRep);

        // After adding the group, retrieve the group representation to get the Keycloak ID
        if (response.getStatus() == 201) {  // Status 201 indicates successful creation
            String createdGroupId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            savedGroup.setKeycloakId(createdGroupId);
            groupRepository.save(savedGroup);
        } else {
            throw new RuntimeException("Failed to create group in Keycloak");
        }

        return savedGroup;
    }


    /**
     * Updates an existing group with the provided details.
     *
     * @param groupId      The ID of the group to update.
     * @param groupDetails The Group entity containing updated information.
     * @return The updated Group entity.
     */
    @Transactional
    public Group updateGroup(Long groupId, Group groupDetails) {
        // Fetch the existing group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with ID: " + groupId));

        // Update basic fields
        if (groupDetails.getName() != null) {
            group.setName(groupDetails.getName());
        }
        if (groupDetails.getDescription() != null) {
            group.setDescription(groupDetails.getDescription());
        }
        if (groupDetails.getPaid() != null) {
            group.setPaid(groupDetails.getPaid());
        }
        // Update other fields as necessary

        // Update associated users if provided
        if (groupDetails.getUsers() != null) {
            Set<User> updatedUsers = new HashSet<>(userRepository.findAllById(
                    groupDetails.getUsers().stream().map(User::getId).toList()));
            group.setUsers(updatedUsers);
        }

        // Update associated services if provided
        if (groupDetails.getAccessTokens() != null) {
            group.setAccessTokens(groupDetails.getAccessTokens());
        }

        // Save the updated group to the database
        Group updatedGroup = groupRepository.save(group);

        // Update the group in Keycloak
        updateGroupInKeycloak(updatedGroup);

        return updatedGroup;
    }


    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = getGroupById(groupId);

        // Fetch and delete subscription requests associated with the group
        Set<SubscriptionRequest> subscriptionRequests = group.getSubscriptionRequests();
        if (subscriptionRequests != null && !subscriptionRequests.isEmpty()) {
            subscriptionRequests.clear(); // Due to orphanRemoval=true, this will delete the records
            logger.info("Deleted {} subscription requests associated with Group ID: {}", subscriptionRequests.size(), groupId);
        }

        // Fetch users associated with the group
        Set<User> users = new HashSet<>(group.getUsers());

        for (User user : users) {
            try {
                // Delete user via KeycloakService
                keycloakService.deleteUser(user.getKeycloakId());

                // Log deletion
                logger.info("User with ID: {} deleted successfully.", user.getId());
            } catch (RuntimeException e) {
                logger.error("Error deleting user with ID: {}", user.getId(), e);
                // Decide whether to continue or abort. Here, we choose to continue.
            }
        }

        // Now delete the group from Keycloak
        deleteGroupInKeycloak(group);

        // Finally, delete the group from the database
        groupRepository.delete(group);
        logger.info("Group with ID: {} deleted successfully.", groupId);
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

    /**
     * Updates the group information in Keycloak.
     *
     * @param group The Group entity to update in Keycloak.
     */
    private void updateGroupInKeycloak(Group group) {
        try {
            // Fetch the group representation from Keycloak using keycloakId
            org.keycloak.admin.client.resource.GroupsResource groupsResource =
                    KeycloakConfig.getInstance().realm("mss-authent").groups();
            GroupRepresentation groupRep = groupsResource.group(group.getKeycloakId()).toRepresentation();

            // Update the group fields
            groupRep.setName(group.getName());
            groupRep.setAttributes(Map.of("description", List.of(group.getDescription())));

            // Update the group in Keycloak
            groupsResource.group(group.getKeycloakId()).update(groupRep);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update group in Keycloak: " + e.getMessage(), e);
        }
    }

    private void deleteGroupInKeycloak(Group group) {
        try {
            GroupsResource groupsResource = keycloakConfig.getInstance().realm(KeycloakConfig.realm).groups();
            GroupRepresentation groupRep = groupsResource.group(group.getKeycloakId()).toRepresentation();
            groupsResource.group(group.getKeycloakId()).remove();
            logger.info("Group with Keycloak ID: {} deleted from Keycloak.", group.getKeycloakId());
        } catch (NotFoundException e) {
            logger.warn("Group not found in Keycloak with ID: {}. Proceeding with database deletion.", group.getKeycloakId());
        } catch (Exception e) {
            logger.error("Failed to delete group with Keycloak ID: {}", group.getKeycloakId(), e);
            throw new RuntimeException("Failed to delete group with Keycloak ID: " + group.getKeycloakId(), e);
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
    public void deleteAllAccessTokens(Long groupId) {
        // Fetch the group by ID
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        // Clear all access tokens
        Map<UUID, String> accessTokens = group.getAccessTokens();
        if (accessTokens.isEmpty()) {
            throw new IllegalArgumentException("No access tokens found for group with ID: " + groupId);
        }

        accessTokens.clear();

        // Update the 'tokenGenerated' flag
        group.setTokenGenerated(false);

        // Save the updated group
        groupRepository.save(group);
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

        List<Group> groups = groupRepository.findByUsersContaining(user.get());
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