package com.mss.adminservice.Config;

import lombok.Data;

import java.util.UUID;

@Data
public class UserGroupDTO {
    private Long id; // Ensure this is the normal ID from your database
    private String name;
    private String description;
    private String userName; // This should match the JSON field
    private String firstname;
    private String lastName;
    private String emailId;
    private String password;
    private String groupName;
    private Long groupId;

//    public void setGroupId(Long id) {
 //   }
}
