package com.example.statistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class UserDTO {
    private Long id;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("lastName")
    private String lastName;

    private String email;
    private String password;

    @JsonProperty("keycloakId")
    private String keycloakId;

    private List<String> roles;

    private boolean superUser;

    // Getters and Setters
}
