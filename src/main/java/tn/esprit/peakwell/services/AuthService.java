package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.LoginRequest;
import tn.esprit.peakwell.dto.SignupRequest;
import tn.esprit.peakwell.entities.Role;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.userRepository;
import tn.esprit.peakwell.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    @Autowired
    userRepository userRepository;

    private  final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    public ResponseEntity<?> signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email address is already in use"));
        }

        try {
            User user = new User();
            user.setEmail(signupRequest.getEmail());
            user.setFirstName(signupRequest.getFirstName());
            user.setLastName(signupRequest.getLastName());
            user.setAge(signupRequest.getAge());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            user.setRole(Role.STUDENT);
            user.setEnabled(true);

            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "User registered successfully"));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An unexpected error occurred"));
        }
    }

    @Override
    public ResponseEntity<?> login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = userRepository.findByEmail(loginRequest.getEmail());

            String token = jwtUtils.generateToken(user);

            System.out.println("=== Generated JWT ===");
            System.out.println("Token: " + token);

            String[] parts = token.split("\\.");
            String decodedPayload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            System.out.println("Decoded payload: " + decodedPayload);
            System.out.println("=====================");

            return ResponseEntity.ok(Map.of("token", token));

        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Internal server error",
                            "details", e.getMessage()
                    ));
        }
    }


//    @Value("${keycloak.server-url}")
//    private String serverUrl;
//
//    @Value("${keycloak.realm}")
//    private String realm;
//
//    @Value("${keycloak.client-id}")
//    private String clientId;
//
//
//    @Override
//    public ResponseEntity<?> login(LoginRequest request) {
//
//        String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
//
//        System.out.println(" URL: " + url);
//        System.out.println(" client_id: " + clientId);
//        System.out.println(" username: " + request.getUsername());
//        System.out.println(" password: " + request.getPassword());
//
//        WebClient client = WebClient.create();
//
//        try {
//            Map response = client.post()
//                    .uri(url)
//                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                    .body(BodyInserters.fromFormData("client_id", clientId)
//                            .with("grant_type", "password")
//                            .with("username", request.getUsername())
//                            .with("password", request.getPassword()))
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .block();
//
//            String token = (String) response.get("access_token");
//
//            return ResponseEntity.ok(Map.of("token", token));
//
//        } catch (WebClientResponseException e) {
//
//            try {
//                ObjectMapper mapper = new ObjectMapper();
//                JsonNode errorJson = mapper.readTree(e.getResponseBodyAsString());
//
//                String errorDescription = errorJson.has("error_description")
//                        ? errorJson.get("error_description").asText()
//                        : "Authentication failed";
//
//                //  If 401 → return same message
//                if (e.getStatusCode().value() == 401) {
//                    return ResponseEntity
//                            .status(401)
//                            .body(Map.of("error_description", errorDescription));
//                }
//
//                // Other errors
//                return ResponseEntity
//                        .status(500)
//                        .body(Map.of("error_description", "Something went wrong"));
//
//            } catch (Exception ex) {
//                return ResponseEntity
//                        .status(500)
//                        .body(Map.of("error_description", "Something went wrong"));
//            }
//        }
//    }

}