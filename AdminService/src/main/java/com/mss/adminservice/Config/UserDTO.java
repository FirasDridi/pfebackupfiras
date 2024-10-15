package com.mss.adminservice.Config;

import com.mss.adminservice.Entities.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Set;

@Data
@NoArgsConstructor
public class UserDTO {
    @JsonIgnore // Ignore 'id' during deserialization

    private Long id;
    private String userName;
    private String firstname;
    private String lastName;
    private String email;
    private String password;
    private String keycloakId;
    private Set<String> roles;

    @JsonProperty("superUser")
    private transient boolean isSuperUser;

    public UserDTO(User user) {
        this.id = user.getId();
        this.userName = user.getUsername();
        this.firstname = user.getFirstname();
        this.lastName = user.getLastname();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.keycloakId = user.getKeycloakId();
    }
}
