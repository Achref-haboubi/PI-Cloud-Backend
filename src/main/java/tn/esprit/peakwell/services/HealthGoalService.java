package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.HealthGoalRequest;
import tn.esprit.peakwell.dto.HealthGoalResponse;
import tn.esprit.peakwell.dto.HealthGoalResponse.MilestoneResponse;
import tn.esprit.peakwell.entities.BiometricEntry;
import tn.esprit.peakwell.entities.GoalMilestone;
import tn.esprit.peakwell.entities.HealthGoal;
import tn.esprit.peakwell.entities.MedicalProfile;
import tn.esprit.peakwell.repositories.BiometricEntryRepository;
import tn.esprit.peakwell.repositories.HealthGoalRepository;
import tn.esprit.peakwell.repositories.MedicalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthGoalService {

  private final HealthGoalRepository goalRepo;
  private final BiometricEntryRepository biometricRepo;
  private final MedicalProfileRepository profileRepo;

  private static final Long PROFILE_ID = 1L; // Placeholder until auth exists

  // ── CRUD ────────────────────────────────────────

  public List<HealthGoalResponse> getAllGoals() {
    List<HealthGoal> goals = goalRepo.findAllByOrderByCreatedAtDesc();
    // Auto-update milestones based on current biometric data
    updateAllMilestones(goals);
    return goals.stream().map(this::toResponse).collect(Collectors.toList());
  }

  public List<HealthGoalResponse> getActiveGoals() {
    List<HealthGoal> goals = goalRepo.findByActiveTrueOrderByCreatedAtDesc();
    updateAllMilestones(goals);
    return goals.stream().map(this::toResponse).collect(Collectors.toList());
  }

  @Transactional
  public HealthGoalResponse createGoal(HealthGoalRequest request) {
    MedicalProfile profile = profileRepo.findById(PROFILE_ID).orElse(null);

    HealthGoal goal = HealthGoal.builder()
      .profile(profile)
      .metric(request.getMetric())
      .direction(request.getDirection())
      .startValue(request.getStartValue())
      .targetValue(request.getTargetValue())
      .unit(request.getUnit())
      .deadline(LocalDate.parse(request.getDeadline()))
      .build();

    // Generate milestones
    List<GoalMilestone> milestones = generateMilestones(goal, request.getStartValue(), request.getTargetValue());
    goal.setMilestones(milestones);

    HealthGoal saved = goalRepo.save(goal);

    // Check milestones immediately against current data
    updateMilestones(saved);

    return toResponse(goalRepo.save(saved));
  }

  @Transactional
  public void deleteGoal(Long id) {
    goalRepo.deleteById(id);
  }

  @Transactional
  public HealthGoalResponse deactivateGoal(Long id) {
    HealthGoal goal = goalRepo.findById(id)
      .orElseThrow(() -> new RuntimeException("Goal not found"));
    goal.setActive(false);
    return toResponse(goalRepo.save(goal));
  }

  // ── Milestone Logic ─────────────────────────────

  private List<GoalMilestone> generateMilestones(HealthGoal goal, double start, double target) {
    List<GoalMilestone> milestones = new ArrayList<>();
    double diff = target - start;
    double[] steps = {0.25, 0.5, 0.75, 1.0};
    String[] labels = {"25% milestone", "50% — halfway!", "75% milestone", "Goal reached!"};

    for (int i = 0; i < steps.length; i++) {
      double value = Math.round((start + diff * steps[i]) * 10.0) / 10.0;
      milestones.add(GoalMilestone.builder()
        .goal(goal)
        .label(labels[i])
        .targetValue(value)
        .reached(false)
        .build());
    }
    return milestones;
  }

  private void updateAllMilestones(List<HealthGoal> goals) {
    boolean anyChanged = false;
    for (HealthGoal goal : goals) {
      if (goal.getActive() && !goal.getAchieved()) {
        boolean changed = updateMilestones(goal);
        if (changed) anyChanged = true;
      }
    }
    if (anyChanged) {
      goalRepo.saveAll(goals);
    }
  }

  private boolean updateMilestones(HealthGoal goal) {
    Double currentValue = getCurrentMetricValue(goal.getMetric());
    if (currentValue == null) return false;

    boolean changed = false;
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"));

    for (GoalMilestone m : goal.getMilestones()) {
      boolean shouldBeReached;
      if ("decrease".equals(goal.getDirection())) {
        shouldBeReached = currentValue <= m.getTargetValue();
      } else {
        shouldBeReached = currentValue >= m.getTargetValue();
      }

      if (shouldBeReached && !m.getReached()) {
        m.setReached(true);
        m.setReachedDate(today);
        changed = true;
      }
    }

    // Check if goal is fully achieved
    boolean allReached = goal.getMilestones().stream().allMatch(GoalMilestone::getReached);
    if (allReached && !goal.getAchieved()) {
      goal.setAchieved(true);
      goal.setAchievedDate(LocalDate.now());
      changed = true;
    }

    return changed;
  }

  private Double getCurrentMetricValue(String metric) {
    return biometricRepo.findTopByOrderByRecordedAtDesc()
      .map(entry -> {
        switch (metric) {
          case "weight":     return entry.getWeight();
          case "bmi":        return entry.getBmi();
          case "bodyFat":    return entry.getBodyFat();
          case "muscleMass": return entry.getMuscleMass();
          case "systolic":   return entry.getSystolic() != null ? entry.getSystolic().doubleValue() : null;
          case "glucose":    return entry.getGlucose();
          default:           return null;
        }
      }).orElse(null);
  }

  // ── Mapper ──────────────────────────────────────

  private HealthGoalResponse toResponse(HealthGoal goal) {
    Double currentValue = getCurrentMetricValue(goal.getMetric());

    return HealthGoalResponse.builder()
      .id(goal.getId())
      .metric(goal.getMetric())
      .direction(goal.getDirection())
      .startValue(goal.getStartValue())
      .targetValue(goal.getTargetValue())
      .unit(goal.getUnit())
      .deadline(goal.getDeadline().toString())
      .active(goal.getActive())
      .achieved(goal.getAchieved())
      .achievedDate(goal.getAchievedDate() != null ? goal.getAchievedDate().toString() : null)
      .createdAt(goal.getCreatedAt() != null ? goal.getCreatedAt().toString() : null)
      .milestones(goal.getMilestones().stream()
        .map(m -> MilestoneResponse.builder()
          .id(m.getId())
          .label(m.getLabel())
          .targetValue(m.getTargetValue())
          .reached(m.getReached())
          .reachedDate(m.getReachedDate())
          .build())
        .collect(Collectors.toList()))
      .build();
  }
}
