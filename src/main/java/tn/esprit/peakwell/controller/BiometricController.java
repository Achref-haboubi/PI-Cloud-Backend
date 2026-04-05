package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.BiometricRequest;
import tn.esprit.peakwell.dto.BiometricResponse;
import tn.esprit.peakwell.dto.HealthAlertDto;
import tn.esprit.peakwell.security.JwtUtils;
import tn.esprit.peakwell.services.BiometricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biometrics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class BiometricController {

    private final BiometricService biometricService;
    private final JwtUtils jwtUtils;

    /** Extract userId from JWT, or fall back to 1 if no token present (dev mode). */
    private Long resolveUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                return jwtUtils.extractUserId(authHeader.substring(7));
            } catch (Exception ignored) {}
        }
        return 1L;
    }

    @GetMapping
    public ResponseEntity<List<BiometricResponse>> getAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(biometricService.getAll(resolveUserId(authHeader)));
    }

    @PostMapping
    public ResponseEntity<BiometricResponse> addEntry(
            @Valid @RequestBody BiometricRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(biometricService.addEntry(request, resolveUserId(authHeader)));
    }

    @GetMapping("/latest")
    public ResponseEntity<BiometricResponse> getLatest(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        BiometricResponse latest = biometricService.getLatest(resolveUserId(authHeader));
        return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        biometricService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<BiometricResponse>> getByProfileId(@PathVariable Long profileId) {
        return ResponseEntity.ok(biometricService.getByProfileId(profileId));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<HealthAlertDto>> getAlerts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(biometricService.getAlerts(resolveUserId(authHeader)));
    }
}