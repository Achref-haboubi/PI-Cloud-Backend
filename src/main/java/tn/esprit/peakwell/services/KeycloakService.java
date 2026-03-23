package tn.esprit.peakwell.services;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.peakwell.dto.RegisterRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public String createUser(RegisterRequest request) {

        System.out.println("TOKEN = " + keycloak.tokenManager().getAccessToken().getToken());

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmailVerified(false); // important

        // password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);

        user.setCredentials(List.of(credential));

        // create user
        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Error creating user in Keycloak");
        }

        // extract userId
        String location = response.getHeaderString("Location");
        String userId = location.substring(location.lastIndexOf("/") + 1);

        // assign role
        assignRole(userId, request.getRole());

        // send verification email
        keycloak.realm(realm).users().get(userId).sendVerifyEmail();

        return userId;
    }

    public void deleteUser(String userId) {
        keycloak.realm(realm)
                .users()
                .delete(userId);
    }

    private void assignRole(String userId, String roleName) {

        RoleRepresentation role = keycloak.realm(realm)
                .roles()
                .get(roleName)
                .toRepresentation();

        keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(role));
    }
}
