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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.peakwell.dto.AuthResponse;
import tn.esprit.peakwell.dto.LoginRequest;
import tn.esprit.peakwell.dto.RegisterRequest;
import tn.esprit.peakwell.entities.Role;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.UserRepository;

import java.util.Date;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService{

  private final KeycloakService keycloakService;
  @Autowired
  UserRepository userRepository;

  private final IEmailService emailService;

  @Value("${keycloak.server-url}")
  private String serverUrl;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.client-id}")
  private String clientId;

  @Value("${keycloak.client-secret}")
  private String clientSecret;

  private final RestTemplate restTemplate = new RestTemplate();

  public ResponseEntity<?> login(LoginRequest request) {

    //  Get user from DB
    User user = userRepository.findByEmail(request.getEmail());

    if (user == null) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body("Invalid email or password");
    }

    //  Check if disabled by admin
    if (!user.isEnabled()) {
      return ResponseEntity
              .status(HttpStatus.FORBIDDEN)
              .body("Account is disabled by admin");
    }

    //  Check if locked
    if (isLocked(user)) {
      return ResponseEntity
              .status(HttpStatus.FORBIDDEN)
              .body("Account locked for 1 hour due to multiple failed attempts");
    }

    //  Call Keycloak
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

      //  SUCCESS  reset attempts
      handleSuccessLogin(user);

      AuthResponse auth = new AuthResponse();
      auth.setAccessToken((String) res.get("access_token"));
      auth.setRefreshToken((String) res.get("refresh_token"));
      auth.setExpiresIn((Integer) res.get("expires_in"));

      return ResponseEntity.ok(auth);

    } catch (HttpClientErrorException e) {

      //  WRONG PASSWORD
      if (e.getStatusCode().value() == 401) {

        handleFailedLogin(user);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
      }

      return ResponseEntity
              .status(e.getStatusCode())
              .body("Client error from Keycloak");

    } catch (Exception e) {

      return ResponseEntity
              .status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Authentication server error");
    }
  }


  @Override
  public ResponseEntity<?> register(RegisterRequest request) {

    String keycloakId = null;

    try {

      keycloakId = keycloakService.createUser(request);

      System.out.println("EMAIL FROM FRONT: " + request.getEmail());
      User user = new User();
      user.setKeycloakId(keycloakId);
      user.setEmail(request.getEmail());
      user.setFirstName(request.getFirstName());
      user.setLastName(request.getLastName());
      user.setRole(Role.valueOf(request.getRole()));

      userRepository.save(user);

      return ResponseEntity.status(201).body(
              Map.of("message", "User registered successfully")
      );

    } catch (Exception e) {

      e.printStackTrace();

      // rollback
      if (keycloakId != null) {
        try {
          keycloakService.deleteUser(keycloakId);
        } catch (Exception ex) {
          System.err.println("Rollback failed: " + ex.getMessage());
        }
      }

      String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

      if (errorMessage.contains("409") || errorMessage.contains("exists")) {
        return ResponseEntity.status(409).body(Map.of("message", "User already exists"));
      }

      if (errorMessage.contains("401")) {
        return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
      }

      if (errorMessage.contains("invalid")) {
        return ResponseEntity.status(400).body(Map.of("message", "Invalid data"));
      }

      return ResponseEntity.status(500).body(Map.of("message", "Server error"));
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


  public void handleFailedLogin(User user) {

    user.setFailedAttempts(user.getFailedAttempts() + 1);
    user.setTotalFailedAttempts(user.getTotalFailedAttempts() + 1);

    if (user.getFailedAttempts() >= 3) {

      user.setAccountLocked(true);
      user.setLockTime(new Date());

      //  SEND LOCK EMAIL
      emailService.sendAccountLockedEmail( user );
    }

    userRepository.save(user);
  }

  public void handleSuccessLogin(User user) {
    user.setFailedAttempts(0);
    user.setAccountLocked(false);
    user.setLockTime(null);

    userRepository.save(user);
  }

  public boolean isLocked(User user) {

    if (!user.isAccountLocked()) return false;

    long ONE_HOUR = 5 * 60 * 1000;

    if (user.getLockTime() == null) return false;
    long diff = new Date().getTime() - user.getLockTime().getTime();

    if (diff > ONE_HOUR) {

      user.setAccountLocked(false);
      user.setFailedAttempts(0);
      user.setLockTime(null);

      userRepository.save(user);

      //  SEND UNLOCK EMAIL
      //emailService.sendAccountUnlockedEmail(user);

      return false;
    }

    return true;
  }

}