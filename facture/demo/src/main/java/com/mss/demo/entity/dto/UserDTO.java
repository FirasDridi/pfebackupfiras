package com.mss.demo.entity.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDTO {
    private String id; // String type
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private Long groupId;
    private List<GroupDTO> groups;
    private String keycloakId; // Added keycloakId field
}
