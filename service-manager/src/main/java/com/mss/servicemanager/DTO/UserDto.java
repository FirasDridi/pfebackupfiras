package com.mss.servicemanager.DTO;

import lombok.Data;

import java.util.UUID;

@Data

public class UserDto {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    // other fields...
}
