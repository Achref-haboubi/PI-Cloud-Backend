package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.DietitianProfileRequest;
import tn.esprit.peakwell.entities.Dietitian;
import tn.esprit.peakwell.services.IDietitianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/dietitian")
public class DietitianController {
    @Autowired
    IDietitianService dietitianService;

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody DietitianProfileRequest request) {

        try {
            String token = authHeader.substring(7);

            // 🔥 now service returns Dietitian
            Dietitian dietitian = dietitianService.completeDietitianProfile(token, request);

            return ResponseEntity.ok(Map.of(
                    "message", "Dietitian profile completed",
                    "data", Map.of(

                            "specialization", dietitian.getSpecialization(),
                            "certification", dietitian.getCertification(),
                            "linkUrl", dietitian.getLinkUrl(),
                            "experienceYears", dietitian.getExperienceYears(),
                            "consultationPrice", dietitian.getConsultationPrice()
                    )
            ));

        } catch (RuntimeException e) {

            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(401).body(
                        Map.of(
                                "message", "Access denied",
                                "details", e.getMessage()
                        )
                );
            }

            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message", "Profile already exists",
                                "details", e.getMessage()
                        )
                );
            }

            return ResponseEntity.status(500).body(
                    Map.of(
                            "message", "Internal server error",
                            "details", e.getMessage()
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.status(500).body(
                    Map.of(
                            "message", "Internal server error",
                            "details", e.getMessage()
                    )
            );
        }
    }
}
