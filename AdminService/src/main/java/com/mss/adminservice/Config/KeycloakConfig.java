package com.mss.adminservice.Config;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Service
public class KeycloakConfig {

    static Keycloak keycloak = null;
    final static String serverUrl = "http://localhost:8088";
    public final static String realm = "mss-authent";
    final static String clientId = "spring-boot-client";
    final static String clientSecret = "8ycLZaqgKIzkHewKwv5OJFdv4v7TR2St";
    final static String userName = "admin";
    final static String password = "admin";


    public KeycloakConfig() {
    }

    public static Keycloak getInstance(){
        if(keycloak == null){

            keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .username(userName)
                    .password(password)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .resteasyClient(new ResteasyClientBuilder()
                            .connectionPoolSize(10)
                            .build()
                    )
                    .build();
        }
        return keycloak;
    }







    /** login attemmp**/






    public boolean logout(String username) {
        try {
            Keycloak keycloak = getInstance();
            System.out.println(getInstance());

            // Fetch the user by username
            UsersResource usersResource = keycloak.realm(realm).users();
            List<UserRepresentation> users = usersResource.search(username);

            if (users != null && !users.isEmpty()) {
                UserRepresentation user = users.get(0);

                // Get the user resource
                UserResource userResource = usersResource.get(user.getId());

                // Revoke tokens for the user
                userResource.logout();

                return true; // Déconnexion réussie
            } else {
                System.err.println("User not found: " + username);
                return false; // Échec de la déconnexion
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Échec de la déconnexion en raison d'une exception
        }
    }


    public void addUser(UserDTO userDTO) {
        // Create the user
        Keycloak kc =getInstance();
        CredentialRepresentation credential = Credentials.createPasswordCredentials(userDTO.getPassword());
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUserName());
        user.setFirstName(userDTO.getFirstname());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setCredentials(Collections.singletonList(credential));
        user.setEnabled(true);

        UsersResource usersResource = kc.realm("mss-authent").users();
        Response response = usersResource.create(user);

        if (response.getStatus() == 201) {
            // Extract the user ID from the response location
           // String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // Get the role resource
            RealmResource realmResource = keycloak.realm("mss-authent");
           // RolesResource rolesResource = realmResource.roles();
            // Get the specific role "employee"
           // RoleRepresentation employeeRole = rolesResource.get("employee").toRepresentation();

            // Get the user resource for the newly created user
         //   UserResource userResource = usersResource.get(userId);
            // Assign the role to the user
          //  userResource.roles().realmLevel().add(Collections.singletonList(employeeRole));
        } else {
            throw new RuntimeException("Failed to create user: " + response.getStatus());
        }
    }




}
