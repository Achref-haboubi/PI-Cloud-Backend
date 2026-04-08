package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.ConsultationRequest;
import tn.esprit.peakwell.dto.ConsultationResponse;
import tn.esprit.peakwell.entities.Consultation;
import tn.esprit.peakwell.repositories.DietitianRepository;
import tn.esprit.peakwell.services.AutoApprovalService;
import tn.esprit.peakwell.services.ConsultationService;
import tn.esprit.peakwell.services.SlotSuggestionService;
import tn.esprit.peakwell.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ConsultationController {

  private final ConsultationService   consultService;
  private final SlotSuggestionService slotService;
  private final AutoApprovalService   autoApprovalService;
  private final DietitianRepository   dietitianRepo;
  private final JwtUtils jwtUtils;

  @Value("${app.base-url:http://localhost:8090/peakwell}")
  private String baseUrl;

  /** Returns the dietitian id from the JWT, or the first dietitian in DB if no token (dev/demo mode). */
  private Long dietitianId(HttpServletRequest req) {
    String auth = req.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer "))
      return jwtUtils.extractUserId(auth.substring(7));
    return dietitianRepo.findFirstBy()
        .map(d -> d.getId())
        .orElseThrow(() -> new RuntimeException("No dietitian found in database"));
  }

  @GetMapping
  public ResponseEntity<List<ConsultationResponse>> getAll(HttpServletRequest req) {
    return ResponseEntity.ok(consultService.getAll(dietitianId(req)));
  }

  @GetMapping("/upcoming")
  public ResponseEntity<List<ConsultationResponse>> getUpcoming() {
    return ResponseEntity.ok(consultService.getUpcoming());
  }

  @GetMapping("/past")
  public ResponseEntity<List<ConsultationResponse>> getPast() {
    return ResponseEntity.ok(consultService.getPast());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ConsultationResponse> getById(@PathVariable Long id) {
    return ResponseEntity.ok(consultService.getById(id));
  }

  @PostMapping
  public ResponseEntity<ConsultationResponse> book(@RequestBody ConsultationRequest request) {
    return ResponseEntity.ok(consultService.book(request));
  }

  @PatchMapping("/{id}/notes")
  public ResponseEntity<ConsultationResponse> addNotes(@PathVariable Long id,
                                                       @RequestBody ConsultationRequest request) {
    return ResponseEntity.ok(consultService.addNotes(id, request));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<ConsultationResponse> updateStatus(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
    return ResponseEntity.ok(consultService.updateStatus(id, body.get("status")));
  }

  @PostMapping("/{id}/feedback")
  public ResponseEntity<ConsultationResponse> submitFeedback(@PathVariable Long id,
                                                             @RequestBody Map<String, Object> feedback) {
    return ResponseEntity.ok(consultService.submitFeedback(id, feedback));
  }

  @GetMapping("/compare")
  public ResponseEntity<Map<String, Object>> compare(@RequestParam Long id1, @RequestParam Long id2) {
    return ResponseEntity.ok(consultService.compareConsultations(id1, id2));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancel(@PathVariable Long id) {
    consultService.cancel(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/doctor")
  public ResponseEntity<ConsultationResponse> changeDietitian(@PathVariable Long id,
                                                               @RequestBody Map<String, String> body) {
    return ResponseEntity.ok(consultService.changeDietitian(id, body.get("doctorName"), body.get("doctorSpecialty")));
  }

  @PatchMapping("/{id}/details")
  public ResponseEntity<ConsultationResponse> updateDetails(@PathVariable Long id,
                                                             @RequestBody ConsultationRequest request) {
    return ResponseEntity.ok(consultService.updateDetails(id, request));
  }

  @PatchMapping("/{id}/reschedule")
  public ResponseEntity<ConsultationResponse> reschedule(@PathVariable Long id,
                                                         @RequestBody Map<String, String> body) {
    return ResponseEntity.ok(consultService.reschedule(id, body.get("scheduledAt")));
  }
  @PostMapping("/{id}/rating")
  public ResponseEntity<ConsultationResponse> saveRating(@PathVariable Long id,
                                                         @RequestBody Map<String, Object> data) {
    return ResponseEntity.ok(consultService.saveRating(id, data));
  }

  @GetMapping("/pending")
  public ResponseEntity<List<ConsultationResponse>> getPending(HttpServletRequest req) {
    return ResponseEntity.ok(consultService.getPending(dietitianId(req)));
  }

  @PatchMapping("/{id}/confirm")
  public ResponseEntity<ConsultationResponse> confirm(@PathVariable Long id, HttpServletRequest req) {
    return ResponseEntity.ok(consultService.confirm(id, dietitianId(req)));
  }

  /**
   * Reject a consultation.
   * Body: { "reason": "...", "suggestedSlots": ["2026-04-10T09:00", ...] }
   * If suggestedSlots is provided, SuggestedSlot records are created and an
   * email with confirmation links is sent to the patient.
   */
  @PatchMapping("/{id}/reject")
  public ResponseEntity<?> reject(@PathVariable Long id,
                                  @RequestBody Map<String, Object> body,
                                  HttpServletRequest req) {
    Long did = dietitianId(req);

    String reason = (String) body.getOrDefault("reason", "");

    @SuppressWarnings("unchecked")
    List<String> suggestedSlots = body.containsKey("suggestedSlots")
        ? (List<String>) body.get("suggestedSlots")
        : List.of();

    ConsultationResponse rejected = consultService.reject(id, reason, did);

    if (!suggestedSlots.isEmpty()) {
      // Reload the full entity to pass to SlotSuggestionService
      slotService.createSuggestionsAndNotify(
          consultService.findEntity(id),
          suggestedSlots,
          reason,
          baseUrl
      );
    }

    return ResponseEntity.ok(rejected);
  }

  /**
   * GET /api/consultations/{id}/available-slots
   * Returns up to 12 free ISO-datetime slots for the dietitian owning this consultation.
   */
  @GetMapping("/{id}/available-slots")
  public ResponseEntity<?> availableSlots(@PathVariable Long id, HttpServletRequest req) {
    Long did = dietitianId(req);
    if (did == null) return ResponseEntity.status(401).body("Authentication required");
    List<String> slots = slotService.getAvailableSlots(did, 12);
    return ResponseEntity.ok(slots);
  }

  /**
   * POST /api/consultations/auto-approval/run
   * Manually triggers the auto-approval job — use for testing without waiting 10 min.
   */
  @PostMapping("/auto-approval/run")
  public ResponseEntity<Map<String, Object>> triggerAutoApproval() {
    return ResponseEntity.ok(autoApprovalService.runNow());
  }

  /**
   * GET /api/consultations/confirm-slot?token=UUID
   * Public endpoint — called when patient clicks the email link.
   * Returns an HTML confirmation page.
   */
  @GetMapping("/confirm-slot")
  public ResponseEntity<String> confirmSlot(@RequestParam String token) {
    try {
      Consultation c = slotService.confirmSlot(token);
      String html = """
          <!DOCTYPE html>
          <html lang="fr">
          <head><meta charset="UTF-8"><title>Rendez-vous confirmé · PeakWell</title>
          <style>
            body{margin:0;padding:40px;background:#f5f1ed;font-family:'Segoe UI',sans-serif;display:flex;
                 justify-content:center;align-items:center;min-height:100vh;box-sizing:border-box;}
            .card{background:#fff;border-radius:20px;padding:48px 56px;max-width:480px;text-align:center;
                  box-shadow:0 8px 32px rgba(0,0,0,0.08);}
            .icon{font-size:56px;margin-bottom:16px;}
            h1{font-size:24px;color:#1e1a16;margin:0 0 12px;}
            p{font-size:15px;color:#5a5450;line-height:1.7;margin:0 0 8px;}
            .date{font-size:18px;font-weight:700;color:#c96a3f;margin:16px 0;}
            .pill{display:inline-block;background:rgba(122,158,126,0.12);color:#7a9e7e;
                  border-radius:100px;padding:6px 20px;font-size:13px;font-weight:600;margin-top:8px;}
          </style></head>
          <body>
            <div class="card">
              <div class="icon">🌿</div>
              <h1>Rendez-vous confirmé !</h1>
              <p>Votre consultation avec <strong>%s</strong></p>
              <div class="date">%s</div>
              <p>a bien été enregistrée.</p>
              <div class="pill">✅ Confirmé</div>
              <p style="margin-top:24px;font-size:13px;color:#b5aaa5;">
                Vous recevrez un rappel par email 24h avant votre rendez-vous.
              </p>
            </div>
          </body></html>
          """.formatted(
              c.getDoctorName(),
              c.getScheduledAt().format(java.time.format.DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", java.util.Locale.FRENCH))
          );
      return ResponseEntity.ok().header("Content-Type", "text/html;charset=UTF-8").body(html);
    } catch (RuntimeException e) {
      String html = """
          <!DOCTYPE html>
          <html lang="fr">
          <head><meta charset="UTF-8"><title>Erreur · PeakWell</title>
          <style>
            body{margin:0;padding:40px;background:#f5f1ed;font-family:'Segoe UI',sans-serif;display:flex;
                 justify-content:center;align-items:center;min-height:100vh;box-sizing:border-box;}
            .card{background:#fff;border-radius:20px;padding:48px 56px;max-width:480px;text-align:center;
                  box-shadow:0 8px 32px rgba(0,0,0,0.08);}
            .icon{font-size:56px;margin-bottom:16px;}
            h1{font-size:24px;color:#1e1a16;margin:0 0 12px;}
            p{font-size:15px;color:#5a5450;line-height:1.7;margin:0;}
            .pill{display:inline-block;background:rgba(201,106,63,0.12);color:#c96a3f;
                  border-radius:100px;padding:6px 20px;font-size:13px;font-weight:600;margin-top:16px;}
          </style></head>
          <body>
            <div class="card">
              <div class="icon">⚠️</div>
              <h1>Lien invalide</h1>
              <p>%s</p>
              <div class="pill">Contactez votre nutritionniste</div>
            </div>
          </body></html>
          """.formatted(e.getMessage());
      return ResponseEntity.badRequest().header("Content-Type", "text/html;charset=UTF-8").body(html);
    }
  }

  @GetMapping("/reminders")
  public ResponseEntity<List<Map<String, Object>>> getReminders() {
    return ResponseEntity.ok(consultService.getReminders());
  }
}