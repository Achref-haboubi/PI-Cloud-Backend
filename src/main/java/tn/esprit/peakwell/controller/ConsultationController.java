package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.ConsultationRequest;
import tn.esprit.peakwell.dto.ConsultationResponse;
import tn.esprit.peakwell.services.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ConsultationController {

  private final ConsultationService consultService;

  @GetMapping
  public ResponseEntity<List<ConsultationResponse>> getAll() {
    return ResponseEntity.ok(consultService.getAll());
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
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    consultService.delete(id);
    return ResponseEntity.noContent().build();
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

  @GetMapping("/reminders")
  public ResponseEntity<List<Map<String, Object>>> getReminders() {
    return ResponseEntity.ok(consultService.getReminders());
  }
}
