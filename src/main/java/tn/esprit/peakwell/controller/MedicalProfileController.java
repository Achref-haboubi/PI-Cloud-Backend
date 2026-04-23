package tn.esprit.peakwell.controller;


import tn.esprit.peakwell.dto.MedicalProfileRequest;
import tn.esprit.peakwell.dto.MedicalProfileResponse;
import tn.esprit.peakwell.services.MedicalProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MedicalProfileController {

    private final MedicalProfileService profileService;

    @GetMapping
    public ResponseEntity<MedicalProfileResponse> getProfile() {
        MedicalProfileResponse profile = profileService.getProfile();
        return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<MedicalProfileResponse> saveProfile(@RequestBody MedicalProfileRequest request) {
        return ResponseEntity.ok(profileService.saveProfile(request));
    }

    @PutMapping
    public ResponseEntity<MedicalProfileResponse> updateProfile(@RequestBody MedicalProfileRequest request) {
        return ResponseEntity.ok(profileService.saveProfile(request));
    }
}
