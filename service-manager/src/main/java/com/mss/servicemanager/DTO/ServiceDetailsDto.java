package com.mss.servicemanager.DTO;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ServiceDetailsDto {
    private UUID id;
    private String name;
    private String description;
    private String version;
    private String endpoint;
    private boolean status;
    private String configuration;
    private String pricing;
    private List<GroupDto> groups;
    private List<UserDto> users;
}
