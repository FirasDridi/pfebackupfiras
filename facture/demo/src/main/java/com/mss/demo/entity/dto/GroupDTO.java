package com.mss.demo.entity.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class GroupDTO {
    private Long id;
    private String name;
    private String keycloakId;
    private Map<UUID, String> accessTokens; // Updated field
    private boolean tokenGenerated;
    private String description;
    private Boolean paid;
    private List<UserDTO> users;
}
