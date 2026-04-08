package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.services.SymptomPredictorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predict")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class SymptomPredictorController {

  private final SymptomPredictorService predictorService;

  @PostMapping("/severity")
  public ResponseEntity<Map<String, Object>> predictSeverity(@RequestBody Map<String, Object> request) {
    int stress = ((Number) request.getOrDefault("stressLevel", 3)).intValue();
    int mood = ((Number) request.getOrDefault("mood", 3)).intValue();
    int energy = ((Number) request.getOrDefault("energyLevel", 3)).intValue();
    double sleep = ((Number) request.getOrDefault("sleepHours", 7.0)).doubleValue();
    int water = ((Number) request.getOrDefault("waterIntakeMl", 2000)).intValue();
    String time = (String) request.getOrDefault("timeOfDay", "morning");
    List<String> symptoms = (List<String>) request.getOrDefault("symptoms", List.of());
    List<String> triggers = (List<String>) request.getOrDefault("triggers", List.of());
    int age = ((Number) request.getOrDefault("age", 30)).intValue();
    double bmi = ((Number) request.getOrDefault("bmi", 24.0)).doubleValue();
    boolean chronic = Boolean.TRUE.equals(request.getOrDefault("hasChronicCondition", false));
    double exerciseHrs = ((Number) request.getOrDefault("exerciseHoursWeekly", 3.0)).doubleValue();
    int caffeineCups = ((Number) request.getOrDefault("caffeineCupsDaily", 2)).intValue();

    Map<String, Object> prediction = predictorService.predictMultiple(
      stress, mood, energy, sleep, water, time, symptoms, triggers,
      age, bmi, chronic, exerciseHrs, caffeineCups);

    return ResponseEntity.ok(prediction);
  }

  @GetMapping("/status")
  public ResponseEntity<Map<String, Object>> getModelStatus() {
    return ResponseEntity.ok(Map.of(
      "modelLoaded", predictorService.isModelLoaded(),
      "symptomTypes", predictorService.getSymptomTypes(),
      "triggerNames", predictorService.getTriggerNames(),
      "severityLabels", predictorService.getSeverityLabels()
    ));
  }
}
