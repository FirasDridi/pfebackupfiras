package com.mss.adminservice.Config;

import com.mss.adminservice.Entities.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String userName;
    private String firstname;
    private String lastName;
    private String email;
    private String password;
    private String keycloakId; // Add this line
    private Set<String> roles;  // Add this field to store roles

    public UserDTO(User user) {
        this.id = user.getId();
        this.userName = user.getUsername();
        this.firstname = user.getFirstname();
        this.lastName = user.getLastname();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.keycloakId = user.getKeycloakId(); // Add this line
    }
}
