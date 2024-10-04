package com.mss.adminservice.Service;

import com.mss.adminservice.Config.KeycloakConfig;
import com.mss.adminservice.Config.UserDTO;
import com.mss.adminservice.Config.UserGroupDTO;
import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.Role;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

// Other imports

@Service
@AllArgsConstructor
public class KeyCloakService {

    private static final Logger logger = LoggerFactory.getLogger(KeyCloakService.class);
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final KeycloakConfig keycloakConfig;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
        credential.setTemporary(true);  // Set the password as temporary
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


    @Transactional
    public void deleteUser(String keycloakId) {
        try {
            logger.info("Attempting to delete user with Keycloak ID: {}", keycloakId);

            // Delete user from Keycloak
            UsersResource usersResource = keycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
            usersResource.get(keycloakId).remove();
            logger.info("User with Keycloak ID: {} deleted from Keycloak.", keycloakId);

            // Find user in the database
            Optional<User> optionalUser = userRepository.findByKeycloakId(keycloakId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                // Remove user from all associated groups
                Set<Group> groups = new HashSet<>(user.getGroups());
                for (Group group : groups) {
                    group.getUsers().remove(user);
                    groupRepository.save(group); // Persist changes
                }

                // Due to CascadeType.ALL and orphanRemoval=true, notifications are deleted automatically
                userRepository.delete(user);
                logger.info("User with Keycloak ID: {} deleted from the database.", keycloakId);
            } else {
                logger.warn("User not found in database with Keycloak ID: {}", keycloakId);
            }
        } catch (NotFoundException e) {
            // Handle case where user doesn't exist in Keycloak
            logger.warn("User not found in Keycloak with ID: {}. Proceeding with group deletion.", keycloakId);
            // Optionally, you can still attempt to delete from the database if necessary
            Optional<User> optionalUser = userRepository.findByKeycloakId(keycloakId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                Set<Group> groups = new HashSet<>(user.getGroups());
                for (Group group : groups) {
                    group.getUsers().remove(user);
                    groupRepository.save(group);
                }
                userRepository.delete(user);
                logger.info("User with Keycloak ID: {} deleted from the database.", keycloakId);
            } else {
                logger.warn("User not found in database with Keycloak ID: {}", keycloakId);
            }
            // Do not rethrow the exception to allow group deletion to continue
        } catch (Exception e) {
            logger.error("Failed to delete user with Keycloak ID: {}", keycloakId, e);
            throw new RuntimeException("Failed to delete user with Keycloak ID: " + keycloakId, e);
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


    /**
     * Retrieves the current authenticated user's Keycloak ID.
     *
     * @return The Keycloak ID (UUID string) of the authenticated user.
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.error("Authentication object is null.");
            throw new RuntimeException("No authentication information found.");
        }

        logger.debug("Authentication type: {}", authentication.getClass().getName());
        if (authentication instanceof KeycloakAuthenticationToken) {
            KeycloakAuthenticationToken keycloakAuth = (KeycloakAuthenticationToken) authentication;
            AccessToken accessToken = keycloakAuth.getAccount().getKeycloakSecurityContext().getToken();
            logger.debug("Access Token Subject: {}", accessToken.getSubject());
            return accessToken.getSubject();
        } else {
            logger.error("Authentication is not an instance of KeycloakAuthenticationToken. Actual type: {}", authentication.getClass().getName());
            throw new RuntimeException("Unable to retrieve Keycloak ID for the authenticated user.");
        }
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

    /**
     * Retrieves user details from Keycloak by user ID.
     *
     * @param userId The Keycloak ID of the user.
     * @return UserDTO containing user details.
     */
    public UserDTO getUserDetailsFromKeycloak(String userId) {
        UsersResource usersResource = keycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
        UserRepresentation userRepresentation = usersResource.get(userId).toRepresentation();

        // Find the local user by keycloakId
        Optional<User> userOptional = userRepository.findByKeycloakId(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found in local database with Keycloak ID: " + userId);
        }
        User user = userOptional.get();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId()); // Database ID
        userDTO.setUserName(userRepresentation.getUsername());
        userDTO.setEmail(userRepresentation.getEmail());
        userDTO.setFirstname(userRepresentation.getFirstName());
        userDTO.setLastName(userRepresentation.getLastName());

        // Fetch roles
        Set<String> roles = getUserRolesFromKeycloak(userId);
        userDTO.setRoles(roles);

        // Set isSuperUser based on roles
        userDTO.setSuperUser(roles.contains("superuser"));

        return userDTO;
    }
    /**
     * Retrieves the roles assigned to a user from Keycloak.
     *
     * @param keycloakId The Keycloak ID of the user.
     * @return A set of role names.
     */
    public Set<String> getUserRolesFromKeycloak(String keycloakId) {
        UsersResource usersResource = keycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
        List<RoleRepresentation> roles = usersResource.get(keycloakId).roles().realmLevel().listEffective();
        return roles.stream().map(RoleRepresentation::getName).collect(Collectors.toSet());
    }
    /**
     * Updates the connected user's profile and password.
     *
     * @param userDTO The user data transfer object containing updated information.
     * @return The updated User entity from the database.
     */
    @Transactional
    public User updateConnectedUser(UserDTO userDTO) {
        try {
            logger.debug("Starting update process for connected user.");

            String keycloakId = getCurrentUserId();
            logger.debug("Retrieved Keycloak ID: {}", keycloakId);

            UsersResource usersResource = keycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
            UserResource userResource = usersResource.get(keycloakId);

            // Fetch current user representation from Keycloak
            UserRepresentation userRepresentation = userResource.toRepresentation();
            logger.debug("Fetched user representation from Keycloak: {}", userRepresentation.getUsername());

            // Update fields
            userRepresentation.setFirstName(userDTO.getFirstname());
            userRepresentation.setLastName(userDTO.getLastName());
            userRepresentation.setEmail(userDTO.getEmail());

            // Update user in Keycloak
            userResource.update(userRepresentation);
            logger.info("Updated user information in Keycloak for user ID: {}", keycloakId);

            // Update password in Keycloak if provided
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(userDTO.getPassword());
                credential.setTemporary(false);
                userResource.resetPassword(credential);
                logger.info("Password updated in Keycloak for user ID: {}", keycloakId);
            }

            // Fetch user from local database
            User user = userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new RuntimeException("User not found in database: " + keycloakId));
            logger.debug("Fetched user from local database: {}", user.getUsername());

            // Update local user fields
            user.setFirstname(userDTO.getFirstname());
            user.setLastname(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());

            // Update password in local database if provided
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                logger.info("Password updated in local database for user ID: {}", user.getId());
            }

            // Save updated user to local database
            User updatedUser = userRepository.save(user);
            logger.info("Successfully updated connected user with ID: {}", updatedUser.getId());

            return updatedUser;
        } catch (NotFoundException e) {
            logger.error("User not found in Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException("User not found in Keycloak.", e);
        } catch (Exception e) {
            logger.error("Error updating connected user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update connected user.", e);
        }
    }


    /**
     * Retrieves a user's Keycloak ID based on the authenticated session.
     *
     * @return The Keycloak ID of the current user.

    public String getCcurrentUserId() { // Note: Method name has a typo and should be corrected if used elsewhere
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken) {
            org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken keycloakAuth =
                    (org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken) authentication;
            org.keycloak.representations.AccessToken accessToken =
                    keycloakAuth.getAccount().getKeycloakSecurityContext().getToken();
            return accessToken.getSubject();
        } else {
            throw new RuntimeException("Unable to get Keycloak ID of the authenticated user.");
        }
    }  */
    /**
     * Retrieves the connected user's details, including database ID.
     *
     * @return UserDTO containing user details.
     */
    public UserDTO getConnectedUserDetails() {
        String keycloakId = getCurrentUserId();

        // Fetch user details from Keycloak
        UsersResource usersResource = keycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
        UserRepresentation userRepresentation = usersResource.get(keycloakId).toRepresentation();

        // Fetch user from the local database
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found in database: " + keycloakId));

        // Assemble UserDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId()); // Database ID
        userDTO.setUserName(userRepresentation.getUsername());
        userDTO.setFirstname(userRepresentation.getFirstName());
        userDTO.setLastName(userRepresentation.getLastName());
        userDTO.setEmail(userRepresentation.getEmail());

        // Fetch roles
        Set<String> roles = getUserRolesFromKeycloak(keycloakId);
        userDTO.setRoles(roles);

        // Set isSuperUser based on roles
        userDTO.setSuperUser(roles.contains("superuser"));

        return userDTO;
    }
    @Transactional
    public User updateUserById(String keycloakId, UserDTO userDTO) {
        try {
            logger.debug("Starting update process for user with Keycloak ID: {}", keycloakId);

            // Fetch user from Keycloak
            UsersResource usersResource = keycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
            UserResource userResource = usersResource.get(keycloakId);
            UserRepresentation userRepresentation = userResource.toRepresentation();
            logger.debug("Fetched user representation from Keycloak.");

            // Update user details in Keycloak
            userRepresentation.setFirstName(userDTO.getFirstname());
            userRepresentation.setLastName(userDTO.getLastName());
            userRepresentation.setEmail(userDTO.getEmail());

            userResource.update(userRepresentation);
            logger.info("Updated user information in Keycloak for user ID: {}", keycloakId);

            // Update password in Keycloak if provided
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(userDTO.getPassword());
                credential.setTemporary(false);
                userResource.resetPassword(credential);
                logger.info("Password updated in Keycloak for user ID: {}", keycloakId);
            }

            // Fetch user from local database
            User user = userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new RuntimeException("User not found in database: " + keycloakId));
            logger.debug("Fetched user from local database: {}", user.getUsername());

            // Update local user fields
            user.setFirstname(userDTO.getFirstname());
            user.setLastname(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());

            // Update password in local database if provided
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                logger.info("Password updated in local database for user ID: {}", user.getId());
            }

            // Save updated user to local database
            User updatedUser = userRepository.save(user);
            logger.info("Successfully updated user in local database with ID: {}", updatedUser.getId());

            return updatedUser;
        } catch (NotFoundException e) {
            logger.error("User not found in Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException("User not found in Keycloak.", e);
        } catch (Exception e) {
            logger.error("Error updating user with Keycloak ID: {}: {}", keycloakId, e.getMessage(), e);
            throw new RuntimeException("Failed to update user.", e);
        }
    }

}
