package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.peakwell.dto.AuthResponse;
import tn.esprit.peakwell.dto.LoginRequest;
import tn.esprit.peakwell.dto.RegisterRequest;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.exception.AuthException;
import tn.esprit.peakwell.repositories.UserRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService{

    private final KeycloakService keycloakService;
    @Autowired
    UserRepository userRepository;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AuthResponse login(LoginRequest request) {

        String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", request.getEmail());
        body.add("password", request.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

            Map<String, Object> res = response.getBody();

            AuthResponse auth = new AuthResponse();
            auth.setAccessToken((String) res.get("access_token"));
            auth.setRefreshToken((String) res.get("refresh_token"));
            auth.setExpiresIn((Integer) res.get("expires_in"));

            return auth;

        } catch (org.springframework.web.client.HttpClientErrorException e) {

            //  IMPORTANT: check status code
            if (e.getStatusCode().value() == 401) {
                throw new AuthException("Invalid email or password", 401);
            }

            throw new AuthException("Client error from Keycloak", e.getStatusCode().value());

        } catch (Exception e) {

            throw new AuthException("Authentication server error", 500);
        }
    }

    @Override
    public void register(RegisterRequest request) {

        String keycloakId = null;

        try {
            //  Create user in Keycloak
            keycloakId = keycloakService.createUser(request);

            //  Save in DB
            User user = new User();
            user.setKeycloakId(keycloakId);
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            userRepository.save(user);

        } catch (Exception e) {

            //  If DB fails → rollback Keycloak
            if (keycloakId != null) {
                try {
                    keycloakService.deleteUser(keycloakId);
                } catch (Exception ex) {
                    // log this, don't hide original error
                    System.err.println(" Failed to rollback Keycloak user: " + ex.getMessage());
                }
            }

            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    @Override
    public String getCurrentUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //  No authentication at all
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();

        //  Wrong principal type (not JWT)
        if (!(principal instanceof Jwt jwt)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authentication token"
            );
        }

        //  Missing subject
        String userId = jwt.getClaimAsString("sub");

        if (userId == null || userId.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid token: subject missing"
            );
        }

        return userId;
    }

    @Override
    public void forgotPassword(String email) {
        keycloakService.forgotPassword(email);
    }
}
