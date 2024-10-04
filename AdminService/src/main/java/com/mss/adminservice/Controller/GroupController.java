package com.mss.adminservice.Controller;

import com.mss.adminservice.Config.UserGroupDTO;
import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.User;
import com.mss.adminservice.Repo.GroupRepository;
import com.mss.adminservice.Repo.UserRepository;
import com.mss.adminservice.Service.GroupService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/groups")
@AllArgsConstructor
@CrossOrigin("*")
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;
    private  final GroupRepository groupRepository ;

    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Group> addGroup(@RequestBody UserGroupDTO group) {
        Group savedGroup = groupService.addGroup(new Group(group));
        return ResponseEntity.ok(savedGroup);
    }
    /**
     * Deletes an access token from a specified group.
     *
     * @param groupId   The ID of the group.
     * @return A ResponseEntity with a success or error message.
     */
    @DeleteMapping("/{groupId}/access-tokens")
    public ResponseEntity<Map<String, Object>> deleteAllAccessTokens(@PathVariable Long groupId) {
        Map<String, Object> response = new HashMap<>();
        try {
            groupService.deleteAllAccessTokens(groupId);
            response.put("message", "All access tokens removed successfully from group.");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(400).body(response);
        } catch (RuntimeException e) {
            response.put("message", "An error occurred while deleting access tokens: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping(value = "/update/{groupId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Group> updateGroup(
            @PathVariable Long groupId,
            @RequestBody Group groupDetails) {

        // Basic validation
        if (groupDetails.getName() == null || groupDetails.getName().isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null or empty");
        }

        // You can add more validations as needed

        Group updatedGroup = groupService.updateGroup(groupId, groupDetails);
        return ResponseEntity.ok(updatedGroup);
    }


    @DeleteMapping("/delete/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<Group>> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/details/{groupId}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Group>> searchGroupsByName(@RequestParam String groupName) {
        List<Group> groups = groupService.searchGroupsByName(groupName);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{groupId}/users")
    public ResponseEntity<List<User>> getUsersByGroup(@PathVariable Long groupId) {
        List<User> users = groupService.getUsersByGroup(groupId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{groupId}/users/{userId}")
    public ResponseEntity<Group> addUserToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        Group group = groupService.addUserToGroup(groupId, userId);
        return ResponseEntity.ok(group);
    }

    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/delete-all")
    public ResponseEntity<Void> deleteAllGroups() {
        groupService.deleteAllGroups();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/toggleToken/{id}")
    public ResponseEntity<Group> toggleToken(@PathVariable Long id, @RequestParam UUID serviceId) {
        Group group = groupService.toggleToken(id, serviceId);
        if (group != null) {
            return ResponseEntity.ok(group);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{groupId}/users/{userId}")
    public ResponseEntity<Boolean> isUserInGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        boolean exists = userRepository.existsByGroups_IdAndId(groupId, userId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{userId}/services/{serviceName}")
    public ResponseEntity<Boolean> doesUserHaveService(@PathVariable Long userId, @PathVariable String serviceName) {
        boolean exists = userRepository.existsByIdAndServicesContaining(userId, serviceName);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{groupId}/service-access/{serviceAccessToken}")
    public ResponseEntity<Boolean> checkServiceAccess(@PathVariable Long groupId, @PathVariable String serviceAccessToken) {
        // Fetch the group by ID
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();
            // Check if the serviceAccessToken exists in the group's accessTokens map
            boolean hasAccess = group.getAccessTokens().containsValue(serviceAccessToken);
            return ResponseEntity.ok(hasAccess);
        } else {
            // Group not found
            return ResponseEntity.notFound().build();
        }
    }
}
