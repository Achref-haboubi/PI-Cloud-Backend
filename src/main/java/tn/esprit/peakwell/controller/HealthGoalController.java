package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.HealthGoalRequest;
import tn.esprit.peakwell.dto.HealthGoalResponse;
import tn.esprit.peakwell.services.HealthGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class HealthGoalController {

  private final HealthGoalService goalService;

  @GetMapping
  public ResponseEntity<List<HealthGoalResponse>> getAll() {
    return ResponseEntity.ok(goalService.getAllGoals());
  }

  @GetMapping("/active")
  public ResponseEntity<List<HealthGoalResponse>> getActive() {
    return ResponseEntity.ok(goalService.getActiveGoals());
  }

  @PostMapping
  public ResponseEntity<HealthGoalResponse> create(@RequestBody HealthGoalRequest request) {
    return ResponseEntity.ok(goalService.createGoal(request));
  }

  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<HealthGoalResponse> deactivate(@PathVariable Long id) {
    return ResponseEntity.ok(goalService.deactivateGoal(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    goalService.deleteGoal(id);
    return ResponseEntity.noContent().build();
  }
}
