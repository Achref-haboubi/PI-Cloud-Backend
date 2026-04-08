package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.DietitianProfileRequest;
import tn.esprit.peakwell.entities.Dietitian;
import tn.esprit.peakwell.repositories.DietitianRepository;
import tn.esprit.peakwell.security.JwtUtils;
import tn.esprit.peakwell.services.AutoApprovalService;
import tn.esprit.peakwell.services.IDietitianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RestController
@RequestMapping("/dietitian")
@CrossOrigin(origins = "http://localhost:4200")
public class DietitianController {
  @Autowired IDietitianService    dietitianService;
  @Autowired DietitianRepository  dietitianRepo;
  @Autowired JwtUtils             jwtUtils;
  @Autowired AutoApprovalService  autoApprovalService;

  private Long resolveUserId(String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer "))
      return jwtUtils.extractUserId(authHeader.substring(7));
    // No token — fall back to the first dietitian in the database (dev/demo mode)
    return dietitianRepo.findFirstBy()
        .map(d -> d.getId())
        .orElseThrow(() -> new RuntimeException("No dietitian found in database"));
  }

  @GetMapping("/all")
  public ResponseEntity<List<Map<String, Object>>> getAllDietitians() {
    return ResponseEntity.ok(dietitianService.getAllDietitians());
  }

  /** GET /dietitian/schedule — get working hours of the current dietitian */
  @GetMapping("/schedule")
  public ResponseEntity<?> getSchedule(
      @RequestHeader(value = "Authorization", required = false) String authHeader) {
    Long userId = resolveUserId(authHeader);
    return dietitianRepo.findById(userId)
      .map(d -> {
        List<String> days = (d.getWorkingDays() != null && !d.getWorkingDays().isEmpty())
            ? d.getWorkingDays()
            : List.of("MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY");
        int start = (d.getWorkStartHour() != null) ? d.getWorkStartHour() : 9;
        int end   = (d.getWorkEndHour()   != null) ? d.getWorkEndHour()   : 17;
        // Guard against corrupted data (reversed hours)
        if (start >= end) { start = 9; end = 17; }
        return ResponseEntity.ok(Map.of(
            "workingDays",   days,
            "workStartHour", start,
            "workEndHour",   end
        ));
      })
      .orElse(ResponseEntity.notFound().build());
  }

  /** PATCH /dietitian/schedule — save working hours */
  @PatchMapping("/schedule")
  @Transactional
  public ResponseEntity<?> saveSchedule(
      @RequestHeader(value = "Authorization", required = false) String authHeader,
      @RequestBody Map<String, Object> body) {
    Long userId = resolveUserId(authHeader);
    Dietitian d = dietitianRepo.findById(userId)
      .orElseThrow(() -> new RuntimeException("Dietitian not found"));

    if (body.containsKey("workingDays")) {
      // Modify the JPA-managed collection in place — replacing it via setter
      // causes JPA to lose track of the @ElementCollection and skip the DB update
      d.getWorkingDays().clear();
      d.getWorkingDays().addAll((List<String>) body.get("workingDays"));
    }
    if (body.containsKey("workStartHour"))
      d.setWorkStartHour(((Number) body.get("workStartHour")).intValue());
    if (body.containsKey("workEndHour"))
      d.setWorkEndHour(((Number) body.get("workEndHour")).intValue());

    dietitianRepo.save(d);

    // Immediately re-evaluate all pending consultations against the new schedule
    autoApprovalService.runNow();

    return ResponseEntity.ok(Map.of("message", "Schedule saved"));
  }

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
