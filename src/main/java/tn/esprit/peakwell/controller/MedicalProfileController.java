package tn.esprit.peakwell.controller;


import tn.esprit.peakwell.dto.MedicalProfileRequest;
import tn.esprit.peakwell.dto.MedicalProfileResponse;
import tn.esprit.peakwell.services.MedicalProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /** GET /api/profile/all — fetch all medical profiles (nutritionist view) */
    @GetMapping("/all")
    public ResponseEntity<List<MedicalProfileResponse>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    /** GET /api/profile/by-student/{studentId} — fetch the profile belonging to a student */
    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<MedicalProfileResponse> getByStudent(@PathVariable Long studentId) {
        MedicalProfileResponse res = profileService.getProfileByStudent(studentId);
        return res != null ? ResponseEntity.ok(res) : ResponseEntity.noContent().build();
    }

    /** GET /api/profile/by-dietitian/{dietitianId} — fetch all profiles assigned to a dietitian */
    @GetMapping("/by-dietitian/{dietitianId}")
    public ResponseEntity<List<MedicalProfileResponse>> getByDietitian(@PathVariable Long dietitianId) {
        return ResponseEntity.ok(profileService.getProfilesByDietitian(dietitianId));
    }

    /** PATCH /api/profile/{id}/assign-dietitian/{dietitianId} — assign a dietitian to a profile */
    @PatchMapping("/{id}/assign-dietitian/{dietitianId}")
    public ResponseEntity<MedicalProfileResponse> assignDietitian(
            @PathVariable Long id, @PathVariable Long dietitianId) {
        MedicalProfileRequest req = new MedicalProfileRequest();
        req.setDietitianId(dietitianId);
        return ResponseEntity.ok(profileService.assignDietitian(id, dietitianId));
    }
}
