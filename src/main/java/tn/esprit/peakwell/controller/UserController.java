package tn.esprit.peakwell.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import tn.esprit.peakwell.dto.CurrentUserDTO;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.dto.UserProfile;
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


    @PostMapping("/complete-profile")
public ResponseEntity<?> completeProfile(
        @ModelAttribute ProfileRequest request,
        @RequestPart(value = "image", required = false) MultipartFile image,
        @RequestPart(value = "certificate", required = false) MultipartFile certificate
) {

    userService.completeProfile(request, image, certificate);

    return ResponseEntity.ok(Map.of("message", "Profile completed successfully"));
}

    
@GetMapping("/me")
public ResponseEntity<?> getCurrentUser() {

    String keycloakId = authService.getCurrentUserId();

    User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    //  Build DTO
    CurrentUserDTO dto = mapToDTO(user);

    //  allow if profile not completed
    if (!dto.isProfileCompleted()) {
        return ResponseEntity.ok(dto);
    }

    //  block if completed but not enabled
    if (!user.isEnabled()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Account pending approval"));
    }

    return ResponseEntity.ok(dto);
}

 @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {

            UserProfile profile = userService.getCurrentUserProfile();
            return ResponseEntity.ok(profile);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");

        }
    }

private boolean isProfileCompleted(User user) {
        if (user.getStudent() != null) {
            return user.isProfileCompleted();
        }
        if (user.getDietitian() != null) {
            return user.isProfileCompleted();
        }
        return false;
    }

    private CurrentUserDTO mapToDTO(User user) {

    String role = "";
    boolean profileCompleted = false;

    if (user.getStudent() != null) {
        role = "STUDENT";
        profileCompleted = user.isProfileCompleted();
    } 
    else if (user.getDietitian() != null) {
        role = "DIETITIAN";
        profileCompleted = user.isProfileCompleted();
    }

    return new CurrentUserDTO(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            role,
            profileCompleted,
            user.isEnabled() 
    );
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
