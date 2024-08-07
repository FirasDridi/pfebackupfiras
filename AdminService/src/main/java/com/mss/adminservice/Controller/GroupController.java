package com.mss.adminservice.Controller;

import com.mss.adminservice.Config.UserGroupDTO;
import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.User;
import com.mss.adminservice.Repo.UserRepository;
import com.mss.adminservice.Service.GroupService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
@AllArgsConstructor
@CrossOrigin("*")
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Group> addGroup(@RequestBody UserGroupDTO group) {
        Group savedGroup = groupService.addGroup(new Group(group));
        return ResponseEntity.ok(savedGroup);
    }

    @PutMapping(value = "/update/{groupId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Group> updateGroup(@PathVariable Long groupId, @RequestBody Group groupDetails) {
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
}
