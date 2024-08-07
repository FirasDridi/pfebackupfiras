package com.mss.adminservice.Service;

import com.mss.adminservice.Config.KeycloakConfig;
import com.mss.adminservice.Config.UserDTO;
import com.mss.adminservice.Config.UserGroupDTO;
import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.User;
import com.mss.adminservice.Repo.GroupRepository;
import com.mss.adminservice.Repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Other imports

@Service
@AllArgsConstructor
public class KeyCloakService {

    private static final Logger logger = LoggerFactory.getLogger(KeyCloakService.class);
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public void addUserToGroup(UserGroupDTO userGroupDTO) {
        // Validate that required fields are not null or empty
        validateUserGroupDTO(userGroupDTO);

        // Proceed with Keycloak user creation
        UsersResource usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
        List<UserRepresentation> existingUsers = usersResource.search(userGroupDTO.getUserName());
        if (!existingUsers.isEmpty()) {
            throw new RuntimeException("User already exists in Keycloak: " + userGroupDTO.getUserName());
        }

        CredentialRepresentation credential = createCredential(userGroupDTO.getPassword());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userGroupDTO.getUserName());
        user.setFirstName(userGroupDTO.getFirstname());
        user.setLastName(userGroupDTO.getLastName());
        user.setEmail(userGroupDTO.getEmailId());
        user.setCredentials(Collections.singletonList(credential));
        user.setEnabled(true);

        Response response = usersResource.create(user);

        if (response.getStatus() == 201) {
            String userId = extractUserIdFromResponse(response);

            logger.info("Searching for group: {}", userGroupDTO.getGroupName());
            GroupRepresentation groupRepresentation = getGroupRepresentation(userGroupDTO.getGroupName());
            if (groupRepresentation != null) {
                usersResource.get(userId).joinGroup(groupRepresentation.getId());
            } else {
                logger.error("Group not found in Keycloak: {}", userGroupDTO.getGroupName());
                throw new RuntimeException("Group not found in Keycloak: " + userGroupDTO.getGroupName());
            }

            User savedUser = saveUserToDatabase(userGroupDTO, userId);
            assignGroupToUserInDatabase(savedUser, userGroupDTO.getGroupName(), groupRepresentation.getId());
        } else {
            logger.error("Failed to create user in Keycloak: {}", response.getStatus());
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatus());
        }
    }


    private void validateUserGroupDTO(UserGroupDTO userGroupDTO) {
        if (userGroupDTO.getFirstname() == null || userGroupDTO.getFirstname().isEmpty()) {
            throw new RuntimeException("Firstname cannot be null or empty");
        }
        if (userGroupDTO.getLastName() == null || userGroupDTO.getLastName().isEmpty()) {
            throw new RuntimeException("Lastname cannot be null or empty");
        }
        if (userGroupDTO.getEmailId() == null || userGroupDTO.getEmailId().isEmpty()) {
            throw new RuntimeException("Email cannot be null or empty");
        }
        if (userGroupDTO.getGroupName() == null || userGroupDTO.getGroupName().isEmpty()) {
            throw new RuntimeException("Group name cannot be null or empty");
        }
    }

    private CredentialRepresentation createCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        return credential;
    }

    private String extractUserIdFromResponse(Response response) {
        return response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
    }

    private GroupRepresentation getGroupRepresentation(String groupName) {
        List<GroupRepresentation> groups = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).groups().groups();
        for (GroupRepresentation group : groups) {
            if (group.getName().equals(groupName)) {
                return group;
            }
        }
        return null;
    }

    private User saveUserToDatabase(UserGroupDTO userGroupDTO, String keycloakId) {
        Optional<User> existingUser = userRepository.findByEmail(userGroupDTO.getEmailId());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists in the database: " + userGroupDTO.getEmailId());
        }

        User user = new User();
        user.setUsername(userGroupDTO.getUserName());
        user.setPassword(userGroupDTO.getPassword());
        user.setEmail(userGroupDTO.getEmailId());
        user.setFirstname(userGroupDTO.getFirstname());
        user.setLastname(userGroupDTO.getLastName());
        user.setKeycloakId(keycloakId);
        return userRepository.save(user);
    }

    private void assignGroupToUserInDatabase(User user, String groupName, String keycloakGroupId) {
        Optional<Group> optionalGroup = groupRepository.findByName(groupName);
        Group group;
        if (optionalGroup.isPresent()) {
            group = optionalGroup.get();
        } else {
            group = new Group();
            group.setName(groupName);
            group.setKeycloakId(keycloakGroupId);
            group = groupRepository.save(group);
        }
        user.getGroups().add(group);
        group.getUsers().add(user);
        userRepository.save(user);
        groupRepository.save(group);
    }

    public User updateUser(UserDTO userDTO) {
        UsersResource usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
        List<UserRepresentation> existingUsers = usersResource.search(userDTO.getUserName());
        if (existingUsers.isEmpty()) {
            throw new RuntimeException("User not found in Keycloak: " + userDTO.getUserName());
        }

        UserRepresentation userRepresentation = existingUsers.get(0);
        userRepresentation.setFirstName(userDTO.getFirstname());
        userRepresentation.setLastName(userDTO.getLastName());
        userRepresentation.setEmail(userDTO.getEmail());

        usersResource.get(userRepresentation.getId()).update(userRepresentation);

        User user = userRepository.findByKeycloakId(userRepresentation.getId())
                .orElseThrow(() -> new RuntimeException("User not found in database: " + userDTO.getUserName()));
        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());

        return userRepository.save(user);
    }


    public void deleteUser(String userId) {
        try {
            // Log the user ID being processed
            logger.info("Attempting to delete user with ID: {}", userId);

            // Check if the user exists in Keycloak
            UsersResource usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
            UserRepresentation user = usersResource.get(userId).toRepresentation();
            if (user == null) {
                logger.error("User not found in Keycloak with ID: {}", userId);
                throw new RuntimeException("User not found in Keycloak with ID: " + userId);
            }

            // Remove the user from Keycloak
            usersResource.get(userId).remove();

            // Remove the user from the database
            userRepository.deleteByKeycloakId(userId);
        } catch (NotFoundException e) {
            logger.error("User not found in Keycloak with ID: {}", userId, e);
            throw new RuntimeException("User not found in Keycloak with ID: " + userId);
        } catch (Exception e) {
            logger.error("Failed to delete user with ID: {}", userId, e);
            throw new RuntimeException("Failed to delete user with ID: " + userId, e);
        }
    }



    public UserRepresentation getUserById(String userId) {
        UsersResource usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
        return usersResource.get(userId).toRepresentation();
    }

    public List<UserRepresentation> searchUsersByName(String userName) {
        UsersResource usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
        return usersResource.search(userName);
    }

    public List<UserRepresentation> getAllUsers() {
        UsersResource usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
        return usersResource.list();
    }

    public void addUsersToExistingGroup(String groupName, List<UserGroupDTO> users) {
        // Validate that the group exists
        GroupRepresentation groupRepresentation = getGroupRepresentation(groupName);
        if (groupRepresentation == null) {
            throw new RuntimeException("Group not found in Keycloak: " + groupName);
        }

        UsersResource usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();

        for (UserGroupDTO userGroupDTO : users) {
            userGroupDTO.setGroupName(groupName); // Set the groupName for each user

            // Validate that required fields are not null or empty
            validateUserGroupDTO(userGroupDTO);

            List<UserRepresentation> existingUsers = usersResource.search(userGroupDTO.getUserName());
            if (!existingUsers.isEmpty()) {
                throw new RuntimeException("User already exists in Keycloak: " + userGroupDTO.getUserName());
            }

            CredentialRepresentation credential = createCredential(userGroupDTO.getPassword());

            UserRepresentation user = new UserRepresentation();
            user.setUsername(userGroupDTO.getUserName());
            user.setFirstName(userGroupDTO.getFirstname());
            user.setLastName(userGroupDTO.getLastName());
            user.setEmail(userGroupDTO.getEmailId());
            user.setCredentials(Collections.singletonList(credential));
            user.setEnabled(true);

            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String userId = extractUserIdFromResponse(response);
                usersResource.get(userId).joinGroup(groupRepresentation.getId());

                User savedUser = saveUserToDatabase(userGroupDTO, userId);
                assignGroupToUserInDatabase(savedUser, groupName, groupRepresentation.getId());
            } else {
                logger.error("Failed to create user in Keycloak: {}", response.getStatus());
                throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatus());
            }
        }
    }


    public String getCurrentUserId() {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
        return accessToken.getSubject();
    }
    public UserDTO findByKeycloakId(String keycloakId) {
        Optional<User> userOptional = userRepository.findByKeycloakId(keycloakId);
        return userOptional.map(UserDTO::new).orElse(null);
    }
    public void addUserWithRole(UserGroupDTO userGroupDTO) {
        // Validate that required fields are not null or empty
        validateUserGroupDTO(userGroupDTO);

        UsersResource usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
        List<UserRepresentation> existingUsers = usersResource.search(userGroupDTO.getUserName());
        if (!existingUsers.isEmpty()) {
            throw new RuntimeException("User already exists in Keycloak: " + userGroupDTO.getUserName());
        }

        CredentialRepresentation credential = createCredential(userGroupDTO.getPassword());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userGroupDTO.getUserName());
        user.setFirstName(userGroupDTO.getFirstname());
        user.setLastName(userGroupDTO.getLastName());
        user.setEmail(userGroupDTO.getEmailId());
        user.setCredentials(Collections.singletonList(credential));
        user.setEnabled(true);

        Response response = usersResource.create(user);

        if (response.getStatus() == 201) {
            String userId = extractUserIdFromResponse(response);

            // Get the role resource
            RolesResource rolesResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).roles();
            RoleRepresentation roleRepresentation = rolesResource.get("superuser").toRepresentation();

            // Assign the role to the user
            UserResource userResource = usersResource.get(userId);
            RoleScopeResource roleScopeResource = userResource.roles().realmLevel();
            roleScopeResource.add(Collections.singletonList(roleRepresentation));

            // Save user to database (optional)
            User savedUser = saveUserToDatabase(userGroupDTO, userId);

            // Assign group to user in database and ensure groupId is set
            Optional<Group> groupOptional = groupRepository.findByName(userGroupDTO.getGroupName());
            if (groupOptional.isPresent()) {
                Group group = groupOptional.get();
                userGroupDTO.setGroupId(group.getId());
                assignGroupToUserInDatabase(savedUser, userGroupDTO.getGroupName(), group.getId().toString());
            } else {
                throw new RuntimeException("Group not found in database: " + userGroupDTO.getGroupName());
            }
        } else {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatus());
        }
    }


}
