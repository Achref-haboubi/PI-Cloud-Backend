package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.peakwell.dto.HealthGoalRequest;
import tn.esprit.peakwell.dto.HealthGoalResponse;
import tn.esprit.peakwell.dto.HealthGoalResponse.MilestoneResponse;
import tn.esprit.peakwell.entities.BiometricEntry;
import tn.esprit.peakwell.entities.GoalMilestone;
import tn.esprit.peakwell.entities.HealthGoal;
import tn.esprit.peakwell.entities.MedicalProfile;
import tn.esprit.peakwell.repositories.BiometricEntryRepository;
import tn.esprit.peakwell.repositories.GoalMilestoneRepository;
import tn.esprit.peakwell.repositories.HealthGoalRepository;
import tn.esprit.peakwell.repositories.MedicalProfileRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthGoalService {

  private final HealthGoalRepository goalRepo;
  private final BiometricEntryRepository biometricRepo;
  private final MedicalProfileRepository profileRepo;
  private final GoalMilestoneRepository milestoneRepo;
  private final NotificationService notificationService;

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

    // Generate milestones (custom or auto)
    List<GoalMilestone> milestones = (request.getCustomMilestones() != null && !request.getCustomMilestones().isEmpty())
            ? request.getCustomMilestones().stream()
            .map(cm -> GoalMilestone.builder().goal(goal).label(cm.getLabel()).targetValue(cm.getTargetValue()).reached(false).build())
            .collect(Collectors.toList())
            : generateMilestones(goal, request.getStartValue(), request.getTargetValue());
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

  @Transactional
  public HealthGoalResponse pauseGoal(Long id, String reason) {
    HealthGoal goal = goalRepo.findById(id).orElseThrow(() -> new RuntimeException("Goal not found"));
    goal.setPaused(true);
    goal.setPauseReason(reason);
    return toResponse(goalRepo.save(goal));
  }

  @Transactional
  public HealthGoalResponse resumeGoal(Long id) {
    HealthGoal goal = goalRepo.findById(id).orElseThrow(() -> new RuntimeException("Goal not found"));
    goal.setPaused(false);
    goal.setPauseReason(null);
    return toResponse(goalRepo.save(goal));
  }

  @Transactional
  public HealthGoalResponse editGoal(Long id, Double targetValue, String deadline) {
    HealthGoal goal = goalRepo.findById(id).orElseThrow(() -> new RuntimeException("Goal not found"));
    if (targetValue != null) goal.setTargetValue(targetValue);
    if (deadline != null) goal.setDeadline(LocalDate.parse(deadline));
    return toResponse(goalRepo.save(goal));
  }

  @Transactional
  public HealthGoalResponse createGoalForProfile(Long profileId, HealthGoalRequest request, String dietitianName) {
    MedicalProfile profile = profileRepo.findById(profileId)
            .orElseThrow(() -> new RuntimeException("Profile not found"));

    HealthGoal goal = HealthGoal.builder()
            .profile(profile)
            .metric(request.getMetric())
            .direction(request.getDirection())
            .startValue(request.getStartValue())
            .targetValue(request.getTargetValue())
            .unit(request.getUnit())
            .deadline(LocalDate.parse(request.getDeadline()))
            .assignedByDietitian(true)
            .assignedByDietitianName(dietitianName)
            .build();

    List<GoalMilestone> milestones = (request.getCustomMilestones() != null && !request.getCustomMilestones().isEmpty())
            ? request.getCustomMilestones().stream()
            .map(cm -> GoalMilestone.builder().goal(goal).label(cm.getLabel()).targetValue(cm.getTargetValue()).reached(false).build())
            .collect(Collectors.toList())
            : generateMilestones(goal, request.getStartValue(), request.getTargetValue());
    goal.setMilestones(milestones);

    HealthGoal saved = goalRepo.save(goal);
    notificationService.notifyGoalAssigned(profile, request.getMetric(), dietitianName);
    return toResponse(saved);
  }

  public List<HealthGoalResponse> getGoalsForProfile(Long profileId) {
    List<HealthGoal> goals = goalRepo.findAllByProfileIdOrderByCreatedAtDesc(profileId);
    updateAllMilestones(goals);
    return goals.stream().map(this::toResponse).collect(Collectors.toList());
  }

  @Transactional
  public MilestoneResponse addMilestoneNote(Long milestoneId, String note) {
    GoalMilestone m = milestoneRepo.findById(milestoneId)
            .orElseThrow(() -> new RuntimeException("Milestone not found"));
    m.setNote(note);
    milestoneRepo.save(m);
    return MilestoneResponse.builder()
            .id(m.getId()).label(m.getLabel()).targetValue(m.getTargetValue())
            .reached(m.getReached()).reachedDate(m.getReachedDate()).note(m.getNote())
            .build();
  }

  public Map<String, Object> getChartData(Long goalId) {
    HealthGoal goal = goalRepo.findById(goalId).orElseThrow(() -> new RuntimeException("Goal not found"));
    List<BiometricEntry> entries = biometricRepo.findAllByProfileIdOrderByRecordedAtAsc(goal.getProfile().getId());

    LocalDate goalStart = goal.getCreatedAt().toLocalDate();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");

    List<Map<String, Object>> points = entries.stream()
            .filter(e -> !e.getRecordedAt().toLocalDate().isBefore(goalStart))
            .map(e -> {
              Double val = getCurrentMetricValueFromEntry(goal.getMetric(), e);
              Map<String, Object> point = new LinkedHashMap<>();
              point.put("date", e.getRecordedAt().toLocalDate().format(fmt));
              point.put("value", val);
              return point;
            })
            .filter(p -> p.get("value") != null)
            .collect(Collectors.toList());

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("points", points);
    result.put("startValue", goal.getStartValue());
    result.put("targetValue", goal.getTargetValue());
    result.put("deadline", goal.getDeadline().format(fmt));
    result.put("unit", goal.getUnit());
    return result;
  }

  private Double getCurrentMetricValueFromEntry(String metric, BiometricEntry e) {
    return switch (metric) {
      case "weight"     -> e.getWeight();
      case "bmi"        -> e.getBmi();
      case "bodyFat"    -> e.getBodyFat();
      case "muscleMass" -> e.getMuscleMass();
      case "systolic"   -> e.getSystolic() != null ? e.getSystolic().doubleValue() : null;
      case "glucose"    -> e.getGlucose();
      default           -> null;
    };
  }

  // ── Milestone Logic ─────────────────────────────

  private List<GoalMilestone> generateMilestones(HealthGoal goal, double start, double target) {
    List<GoalMilestone> milestones = new ArrayList<>();
    double diff = target - start;
    double[] steps = {0.25, 0.5, 0.75, 1.0};
    String[] labels = {"25% milestone", "50% — halfway!", "75% milestone", "Goal reached!"};

    for (int i = 0; i < steps.length; i++) {
      double value = Math.round((start + diff * steps[i]) * 100.0) / 100.0;
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
      if (goal.getActive() && !goal.getAchieved() && !Boolean.TRUE.equals(goal.getPaused())) {
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
      // Notify the assigning dietitian if applicable
      if (Boolean.TRUE.equals(goal.getAssignedByDietitian()) && goal.getProfile() != null) {
        MedicalProfile p = goal.getProfile();
        String patientName = (p.getFirstName() != null ? p.getFirstName() : "")
                + " " + (p.getLastName() != null ? p.getLastName() : "");
        // Find the dietitian by name from the profile's assigned dietitian
        if (p.getAssignedDietitian() != null) {
          notificationService.notifyGoalAchieved(p.getAssignedDietitian(), patientName.trim(), goal.getMetric());
        }
      }
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
            .paused(goal.getPaused())
            .pauseReason(goal.getPauseReason())
            .assignedByDietitian(goal.getAssignedByDietitian())
            .assignedByDietitianName(goal.getAssignedByDietitianName())
            .milestones(goal.getMilestones().stream()
                    .map(m -> MilestoneResponse.builder()
                            .id(m.getId())
                            .label(m.getLabel())
                            .targetValue(m.getTargetValue())
                            .reached(m.getReached())
                            .reachedDate(m.getReachedDate())
                            .note(m.getNote())
                            .build())
                    .collect(Collectors.toList()))
            .build();
  }
}