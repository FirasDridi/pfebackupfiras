package com.example.statistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;
import java.util.List;
@Data
public class UserGroupDTO {
    private Long id;
    private String name;

    @JsonProperty("keycloakId")
    private String keycloakId;

    private boolean tokenGenerated;
    private String description;
    private Boolean paid;
    private Map<String, String> accessTokens;
    private List<UserDTO> users;
    private List<String> roles;

    // Getters and Setters
}
