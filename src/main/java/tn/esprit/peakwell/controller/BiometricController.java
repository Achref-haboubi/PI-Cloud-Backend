package tn.esprit.peakwell.controller;

import org.hibernate.annotations.Cascade;
import tn.esprit.peakwell.dto.BiometricRequest;
import tn.esprit.peakwell.dto.BiometricResponse;
import tn.esprit.peakwell.dto.HealthAlertDto;
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

    @GetMapping
    public ResponseEntity<List<BiometricResponse>> getAll() {
        return ResponseEntity.ok(biometricService.getAll());
    }

    @PostMapping
    public ResponseEntity<BiometricResponse> addEntry(@Valid @RequestBody BiometricRequest request) {
        return ResponseEntity.ok(biometricService.addEntry(request));
    }

    @GetMapping("/latest")
    public ResponseEntity<BiometricResponse> getLatest() {
        BiometricResponse latest = biometricService.getLatest();
        return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        biometricService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<HealthAlertDto>> getAlerts() {
        return ResponseEntity.ok(biometricService.getAlerts());
    }
}
