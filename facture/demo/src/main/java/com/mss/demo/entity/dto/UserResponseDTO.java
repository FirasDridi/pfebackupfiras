package com.mss.demo.entity.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserResponseDTO {
    private UserDTO user;
    private List<GroupDTO> groups;
}
