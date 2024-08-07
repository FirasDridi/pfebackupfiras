package com.mss.adminservice.Controller;

import com.mss.adminservice.Config.UserDTO;
import com.mss.adminservice.Config.UserGroupDTO;
import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.SubscriptionRequest;
import com.mss.adminservice.Entities.User;
import com.mss.adminservice.Repo.GroupRepository;
import com.mss.adminservice.Repo.UserRepository;
import com.mss.adminservice.Service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.annotations.Payant;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
@Tag(name = "Admin Controller", description = "APIs for managing users and groups")
public class AdminController {

    private final KeyCloakService kc;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ServiceAssignmentService serviceAssignmentService;
    private final SubscriptionService subscriptionService;
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private ServiceGroupMappingService serviceGroupMappingService;

    @Autowired
    public AdminController(KeyCloakService kc, UserRepository userRepository, GroupRepository groupRepository, ServiceAssignmentService serviceAssignmentService, SubscriptionService subscriptionService) {
        this.kc = kc;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.serviceAssignmentService = serviceAssignmentService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/addUser")
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody UserGroupDTO userGroupDTO) {
        Map<String, Object> response = new HashMap<>();
        if (userGroupDTO.getGroupName() == null || userGroupDTO.getGroupName().isEmpty()) {
            response.put("message", "Group name is required");
            return ResponseEntity.badRequest().body(response);
        }
        kc.addUserToGroup(userGroupDTO);
        response.put("message", "User added successfully");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateUser")
    public ResponseEntity<User> updateUser(@RequestBody UserDTO userDTO) {
        logger.debug("Updating user with userName: {}", userDTO.getUserName());
        logger.debug("First name: {}", userDTO.getFirstname());
        logger.debug("Last name: {}", userDTO.getLastName());
        logger.debug("Email: {}", userDTO.getEmail());
        logger.debug("Keycloak ID: {}", userDTO.getKeycloakId()); // Add this line

        if (userDTO.getUserName() == null || userDTO.getUserName().isEmpty()) {
            throw new RuntimeException("User name cannot be null or empty");
        }
        if (userDTO.getFirstname() == null || userDTO.getFirstname().isEmpty()) {
            throw new RuntimeException("First name cannot be null or empty");
        }
        if (userDTO.getLastName() == null || userDTO.getLastName().isEmpty()) {
            throw new RuntimeException("Last name cannot be null or empty");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().isEmpty()) {
            throw new RuntimeException("Email cannot be null or empty");
        }

        User updatedUser = kc.updateUser(userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        try {
            kc.deleteUser(userId); // Call the KeycloakService to delete the user from Keycloak
            return ResponseEntity.ok("{\"message\": \"User deleted successfully with ID: " + userId + "\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/getUser/{userId}")
    public ResponseEntity<UserRepresentation> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(kc.getUserById(userId));
    }

    @GetMapping("/searchUsers")
    public ResponseEntity<List<UserRepresentation>> searchUsersByName(@RequestParam String userName) {
        return ResponseEntity.ok(kc.searchUsersByName(userName));
    }

    @GetMapping("/allUsers")
    public ResponseEntity<List<UserRepresentation>> getAllUsers() {
        return ResponseEntity.ok(kc.getAllUsers());
    }

    @PostMapping("/addUsersToGroup")
    public ResponseEntity<Map<String, Object>> addUsersToGroup(@RequestParam String groupName, @RequestBody(required = false) List<UserGroupDTO> users) {
        Map<String, Object> response = new HashMap<>();
        if (groupName == null || groupName.isEmpty()) {
            response.put("message", "Group name is required");
            return ResponseEntity.badRequest().body(response);
        }

        kc.addUsersToExistingGroup(groupName, users);
        response.put("message", "Users added successfully to group: " + groupName);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/userServices/{userId}")
    public ResponseEntity<Set<String>> getUserServices(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Set<String> services = serviceAssignmentService.getServicesForUser(user);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserDTO>> getAllUsersFromRepo() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream().map(user -> {
            UserDTO userDTO = new UserDTO(user);
            userDTO.setId(user.getId());
            userDTO.setUserName(user.getUsername());
            userDTO.setFirstname(user.getFirstname());
            userDTO.setLastName(user.getLastname());
            userDTO.setEmail(user.getEmail());
            userDTO.setPassword(user.getPassword());
            return userDTO;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/usersForService/{serviceId}")
    public ResponseEntity<List<UserDTO>> getUsersForService(@PathVariable UUID serviceId) {
        List<User> users = userRepository.findByServicesContaining(serviceId.toString());
        List<UserDTO> userDTOs = users.stream()
                .map(user -> {
                    UserDTO userDTO = new UserDTO(user);
                    userDTO.setId(user.getId());
                    userDTO.setUserName(user.getUsername());
                    userDTO.setFirstname(user.getFirstname());
                    userDTO.setLastName(user.getLastname());
                    userDTO.setEmail(user.getEmail());
                    return userDTO;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/groupsForService/{serviceId}")
    public ResponseEntity<List<Object>> getGroupsForService(@PathVariable String serviceId) {
        List<Object> groups = groupService.getGroupsForService(serviceId);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/addGroupToService")
    public ResponseEntity<?> addGroupToService(@RequestParam UUID serviceId, @RequestParam Long groupId) {
        try {
            serviceGroupMappingService.addGroupToService(serviceId, groupId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/removeGroupFromService")
    public ResponseEntity<String> removeGroupFromService(@RequestParam UUID serviceId, @RequestParam Long groupId) {
        serviceGroupMappingService.removeGroupFromService(serviceId, groupId);
        return ResponseEntity.ok("Group removed from service successfully.");
    }

    @GetMapping("/hasAccessToService")
    public ResponseEntity<Boolean> hasAccessToService(@RequestParam UUID serviceId, @RequestParam String groupId) {
        boolean hasAccess = serviceGroupMappingService.hasAccessToService(serviceId, groupId);
        return ResponseEntity.ok(hasAccess);
    }

    @GetMapping("/groups/{groupId}/users")
    public ResponseEntity<List<UserDTO>> getUsersByGroup(@PathVariable Long groupId) {
        List<User> users = groupService.getUsersByGroup(groupId);
        List<UserDTO> userDTOs = users.stream().map(user -> {
            UserDTO userDTO = new UserDTO(user);
            userDTO.setId(user.getId());
            userDTO.setUserName(user.getUsername());
            userDTO.setFirstname(user.getFirstname());
            userDTO.setLastName(user.getLastname());
            userDTO.setEmail(user.getEmail());
            return userDTO;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/user/{userId}/details")
    public ResponseEntity<Map<String, Object>> getUserDetailsWithGroup(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        List<Group> groups = user.getGroups().stream().collect(Collectors.toList());

        response.put("user", user);
        response.put("groups", groups);
        response.put("groups", groups);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/groups")
    public ResponseEntity<List<UserGroupDTO>> getUserGroups(@PathVariable String userId) {
        List<UserGroupDTO> groups = groupService.getUserGroupsByUserId(userId);
        if (groups != null) {
            return ResponseEntity.ok(groups);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/details")
    public ResponseEntity<Map<String, Object>> getUserDetails() {
        String userId = kc.getCurrentUserId();
        UserRepresentation user = kc.getUserById(userId);
        List<UserGroupDTO> groups = groupService.getUserGroupsByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("groups", groups);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/info")
    public ResponseEntity<Map<String, Object>> getUserDetailsById(@PathVariable String userId) {
        UserRepresentation user = kc.getUserById(userId);
        List<UserGroupDTO> groups = groupService.getUserGroupsByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("groups", groups);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/dbGroups")
    public ResponseEntity<List<UserGroupDTO>> getUserGroupsFromDB(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        List<UserGroupDTO> groups = user.getGroups().stream().map(group -> {
            UserGroupDTO dto = new UserGroupDTO();
            dto.setGroupName(group.getName());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(groups);
    }

    @GetMapping("/user/{userId}/dbServices")
    public ResponseEntity<Set<String>> getUserServicesFromDB(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Set<String> services = serviceAssignmentService.getServicesForUser(user);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/getUserByKeycloakId/{keycloakId}")
    public ResponseEntity<UserDTO> getUserByKeycloakId(@PathVariable String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUserName(user.getUsername());
        userDTO.setFirstname(user.getFirstname());
        userDTO.setLastName(user.getLastname());
        userDTO.setEmail(user.getEmail());
        userDTO.setKeycloakId(user.getKeycloakId());
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/addUserWithRole")
    public ResponseEntity<Map<String, Object>> addUserWithRole(@RequestBody UserGroupDTO userGroupDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            kc.addUserWithRole(userGroupDTO);
            response.put("message", "User added with role 'superuser' successfully");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error adding user with role 'superuser': " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }






    @GetMapping("/subscriptions/pending/all")
    public ResponseEntity<List<SubscriptionRequest>> getAllPendingRequests() {
        List<SubscriptionRequest> requests = subscriptionService.getAllPendingRequests();
        return ResponseEntity.ok(requests);
    }
    @GetMapping("/validateAccessToken")
    public ResponseEntity<Boolean> validateAccessToken(@RequestParam UUID serviceId, @RequestParam String accessToken) {
        Optional<Group> groupOptional = groupRepository.findByAccessToken(serviceId, accessToken);
        if (groupOptional.isPresent()) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
    }

    @GetMapping("/groupFromToken")
    public ResponseEntity<Long> getGroupIdFromToken(@RequestParam UUID serviceId, @RequestParam String accessToken) {
        Optional<Group> groupOptional = groupRepository.findByAccessToken(serviceId, accessToken);
        if (groupOptional.isPresent()) {
            return ResponseEntity.ok(groupOptional.get().getId());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/group-id-by-token/{token}")
    public ResponseEntity<Long> getGroupIdByToken(@PathVariable String token) {
        Group group = groupRepository.findByAccessTokenValue(token)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return ResponseEntity.ok(group.getId());
    }

    @GetMapping("/user-id-by-token/{token}")
    public ResponseEntity<Long> getUserIdByToken(@PathVariable String token) {
        User user = userRepository.findByKeycloakId(token) // Assuming the token corresponds to the Keycloak ID
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user.getId());
    }

    @GetMapping("/groups/{groupId}/users/{userId}")
    public ResponseEntity<Boolean> isUserInGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        boolean exists = userRepository.existsByGroups_IdAndId(groupId, userId);
        return ResponseEntity.ok(exists);
    }

    @Payant
    @PostMapping("/firastest")
    public ResponseEntity<String> ttc() {
        return ResponseEntity.ok("firas");
    }
}
