package tn.esprit.peakwell.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.UserRepository;
import tn.esprit.peakwell.services.AuthService;
import tn.esprit.peakwell.services.UserService;

import java.util.Map;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {

        String keycloakId = authService.getCurrentUserId();
         System.out.println("keycloak id : " + keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(@RequestBody ProfileRequest request) {

        userService.completeProfile(request);

        return ResponseEntity.ok("Profile completed successfully");
    }

    @PutMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody ProfileRequest request) {

        try {
            User user = userService.updateProfile(request);

            Map<String, Object> response = Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Profile updated successfully",
                    "data", user
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (ResponseStatusException ex) {
            throw ex; // keep existing handled errors (400, 403, etc.)

        } catch (Exception ex) {

            Map<String, Object> response = Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal server error"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
