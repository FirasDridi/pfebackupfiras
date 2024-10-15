package com.mss.adminservice.Service;

import com.mss.adminservice.Controller.AdminController;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Service
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private Keycloak keycloak;
    private String realm = "mss-authent"; // Remplacez par le nom de votre realm
    private String serverUrl = "http://localhost:8088"; // URL de votre serveur Keycloak

    public PasswordResetService() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // Utilisez "master" pour vous authentifier en tant qu'admin
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli") // Client par défaut pour l'admin
                .username("admin") // Nom d'utilisateur admin
                .password("admin") // Mot de passe admin
                .build();
    }

    public void sendResetEmail(String email) {
        UsersResource usersResource = keycloak.realm(realm).users();

        // Rechercher l'utilisateur par e-mail
        List<UserRepresentation> users = usersResource.search(null, null, null, email, 0, 1);

        if (users.isEmpty()) {
            // Pour des raisons de sécurité, ne pas indiquer que l'utilisateur n'existe pas
            return;
        }

        String userId = users.get(0).getId();

        // Envoyer l'e-mail de réinitialisation
        UserResource userResource = usersResource.get(userId);

        // Spécifier l'action de mise à jour du mot de passe
        List<String> actions = Arrays.asList("UPDATE_PASSWORD");

        try {
            // Appeler la méthode sans assignation
            userResource.executeActionsEmail(actions);

            // Si vous le souhaitez, vous pouvez logguer un message indiquant que l'e-mail a été envoyé avec succès
            logger.info("E-mail de réinitialisation envoyé à {}", email);
        } catch (Exception e) {
            // Gérer l'exception en cas d'erreur
            logger.error("Erreur lors de l'envoi de l'e-mail de réinitialisation à {}", email, e);
            throw new RuntimeException("Erreur lors de l'envoi de l'e-mail de réinitialisation");
        }
    }

}
