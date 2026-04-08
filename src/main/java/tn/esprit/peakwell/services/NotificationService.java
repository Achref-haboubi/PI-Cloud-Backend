package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.peakwell.entities.*;
import tn.esprit.peakwell.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notifRepo;
    private final MedicalProfileRepository profileRepo;
    private final BiometricEntryRepository biometricRepo;
    private final HealthGoalRepository goalRepo;
    private final ConsultationRepository consultRepo;
    private final SymptomEntryRepository symptomRepo;

    private static final Long DEFAULT_PROFILE_ID = 1L;

    // ── CRUD ────────────────────────────────────────

    public List<Notification> getAll(Long profileId) {
        return notifRepo.findAllByProfileIdAndDismissedFalseOrderByCreatedAtDesc(
                profileId != null ? profileId : DEFAULT_PROFILE_ID);
    }

    public long getUnreadCount(Long profileId) {
        return notifRepo.countByProfileIdAndReadFalseAndDismissedFalse(
                profileId != null ? profileId : DEFAULT_PROFILE_ID);
    }

    @Transactional
    public Notification markAsRead(Long notifId) {
        Notification n = notifRepo.findById(notifId).orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        n.setReadAt(LocalDateTime.now());
        return notifRepo.save(n);
    }

    @Transactional
    public void markAllAsRead(Long profileId) {
        List<Notification> unread = notifRepo.findAllByProfileIdAndDismissedFalseOrderByCreatedAtDesc(
                profileId != null ? profileId : DEFAULT_PROFILE_ID);
        unread.stream().filter(n -> !n.getRead()).forEach(n -> {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
        });
        notifRepo.saveAll(unread);
    }

    @Transactional
    public void dismiss(Long notifId) {
        Notification n = notifRepo.findById(notifId).orElseThrow();
        n.setDismissed(true);
        notifRepo.save(n);
    }

    @Transactional
    public void dismissAll(Long profileId) {
        List<Notification> all = notifRepo.findAllByProfileIdAndDismissedFalseOrderByCreatedAtDesc(
                profileId != null ? profileId : DEFAULT_PROFILE_ID);
        all.forEach(n -> n.setDismissed(true));
        notifRepo.saveAll(all);
    }

    // ── Manual trigger (called from controller or on biometric save) ──

    @Transactional
    public List<Notification> checkAndNotify(Long profileId) {
        Long pid = profileId != null ? profileId : DEFAULT_PROFILE_ID;
        MedicalProfile profile = profileRepo.findById(pid).orElse(null);
        if (profile == null) return List.of();

        List<Notification> generated = new ArrayList<>();
        List<BiometricEntry> entries = biometricRepo.findAllByProfileIdOrderByRecordedAtAsc(pid);
        BiometricEntry latest = entries.isEmpty() ? null : entries.get(entries.size() - 1);
        BiometricEntry previous = entries.size() >= 2 ? entries.get(entries.size() - 2) : null;

        String patientName = (profile.getFirstName() != null ? profile.getFirstName() : "") + " " +
                (profile.getLastName() != null ? profile.getLastName() : "");

        if (latest == null) return generated;

        // ── 1. CRITICAL RISK — Red Zone Alert ──────────
        double healthScore = computeHealthScore(profile, latest, entries);
        if (healthScore < 35) {
            generated.add(createIfNotExists(profile, "CRITICAL_RISK", "CRITICAL",
                    "🚨 Critical Health Alert",
                    "Your health score is critically low (" + Math.round(healthScore) + "/100). " +
                            "Multiple health indicators need immediate attention. " +
                            "Please book a consultation with your dietitian as soon as possible.",
                    "🚨", "/dossier?tab=consultations", "Book Appointment Now"));
        } else if (healthScore < 50) {
            generated.add(createIfNotExists(profile, "HEALTH_ALERT", "HIGH",
                    "⚠️ Health Score Declining",
                    "Your health score has dropped to " + Math.round(healthScore) + "/100. " +
                            "Consider scheduling a check-up to review your progress.",
                    "⚠️", "/dossier?tab=consultations", "Schedule Check-up"));
        }

        // ── 2. BMI Alert ───────────────────────────────
        if (latest.getBmi() != null && latest.getBmi() > 30) {
            generated.add(createIfNotExists(profile, "HEALTH_ALERT", "HIGH",
                    "⚖️ BMI in Obese Range",
                    "Your current BMI is " + latest.getBmi() + " which is in the obese range. " +
                            "Your dietitian can help adjust your nutrition plan.",
                    "⚖️", "/dossier?tab=dashboard", "View Health Dashboard"));
        }

        // ── 3. Blood Pressure Alert ────────────────────
        if (latest.getSystolic() != null && latest.getSystolic() > 140) {
            generated.add(createIfNotExists(profile, "HEALTH_ALERT", "CRITICAL",
                    "❤️ High Blood Pressure Detected",
                    "Your systolic blood pressure is " + latest.getSystolic() + " mmHg (hypertension range). " +
                            "Please consult your doctor immediately.",
                    "❤️", "/dossier?tab=consultations", "Book Urgent Appointment"));
        } else if (latest.getSystolic() != null && latest.getSystolic() > 130) {
            generated.add(createIfNotExists(profile, "HEALTH_ALERT", "MEDIUM",
                    "❤️ Elevated Blood Pressure",
                    "Your blood pressure is elevated at " + latest.getSystolic() + "/" + latest.getDiastolic() + " mmHg. " +
                            "Monitor closely and consider dietary changes.",
                    "❤️", "/dossier?tab=alerts", "View Details"));
        }

        // ── 4. Glucose Alert ───────────────────────────
        if (latest.getGlucose() != null && latest.getGlucose() > 126) {
            generated.add(createIfNotExists(profile, "HEALTH_ALERT", "CRITICAL",
                    "🩸 Glucose in Diabetic Range",
                    "Your fasting glucose is " + latest.getGlucose() + " mg/dL which indicates diabetes. " +
                            "Urgent consultation recommended.",
                    "🩸", "/dossier?tab=consultations", "Book Appointment"));
        } else if (latest.getGlucose() != null && latest.getGlucose() > 100) {
            generated.add(createIfNotExists(profile, "HEALTH_ALERT", "MEDIUM",
                    "🩸 Pre-diabetic Glucose Level",
                    "Your glucose level is " + latest.getGlucose() + " mg/dL (pre-diabetic range). " +
                            "Dietary adjustments recommended.",
                    "🩸", "/dossier?tab=dashboard", "Review Diet Plan"));
        }

        // ── 5. Rapid Weight Change ─────────────────────
        if (previous != null) {
            double weightDelta = latest.getWeight() - previous.getWeight();
            if (weightDelta > 3) {
                generated.add(createIfNotExists(profile, "HEALTH_ALERT", "HIGH",
                        "⚖️ Rapid Weight Gain",
                        "You've gained " + Math.round(weightDelta * 10.0) / 10.0 + " kg since your last measurement. " +
                                "This could indicate fluid retention or dietary changes that need review.",
                        "⚖️", "/dossier?tab=consultations", "Discuss with Dietitian"));
            }
        }

        // ── 6. Overdue Goals ───────────────────────────
        List<HealthGoal> overdueGoals = goalRepo.findAllByProfileIdOrderByCreatedAtDesc(pid).stream()
                .filter(g -> g.getActive() != null && g.getActive()
                        && g.getDeadline() != null && g.getDeadline().isBefore(LocalDate.now()))
                .toList();
        if (!overdueGoals.isEmpty()) {
            generated.add(createIfNotExists(profile, "GOAL_UPDATE", "MEDIUM",
                    "⏰ " + overdueGoals.size() + " Goal" + (overdueGoals.size() > 1 ? "s" : "") + " Overdue",
                    "You have overdue health goals. Consider updating your targets or booking a consultation to reassess.",
                    "⏰", "/dossier?tab=goals", "Review Goals"));
        }

        // ── 7. Goal Achieved — Celebration! ────────────
        List<HealthGoal> recentlyAchieved = goalRepo.findAllByProfileIdOrderByCreatedAtDesc(pid).stream()
                .filter(g -> g.getAchieved() != null && g.getAchieved()
                        && g.getAchievedDate() != null
                        && g.getAchievedDate().isAfter(LocalDate.now().minusDays(3)))
                .toList();
        for (HealthGoal g : recentlyAchieved) {
            generated.add(createIfNotExists(profile, "GOAL_UPDATE", "LOW",
                    "🎉 Goal Achieved!",
                    "Congratulations! You've achieved your " + g.getDirection() + " " + g.getMetric() + " goal " +
                            "(" + g.getStartValue() + " → " + g.getTargetValue() + " " + g.getUnit() + "). Keep up the great work!",
                    "🎉", "/dossier?tab=goals", "View Goals"));
        }

        // ── 8. No recent entries ───────────────────────
        if (!entries.isEmpty()) {
            long daysSince = ChronoUnit.DAYS.between(entries.get(entries.size() - 1).getRecordedAt(), LocalDateTime.now());
            if (daysSince > 14) {
                generated.add(createIfNotExists(profile, "SYSTEM", "LOW",
                        "📊 Time to Log a Measurement",
                        "It's been " + daysSince + " days since your last biometric entry. " +
                                "Regular tracking helps your dietitian help you better.",
                        "📊", "/dossier?tab=add", "Add Measurement"));
            }
        }

        generated.removeIf(Objects::isNull);
        return generated;
    }

    // ── Scheduled scanner — runs every 6 hours ─────

    @Scheduled(fixedRate = 21600000) // 6 hours
    @Transactional
    public void scheduledScan() {
        log.info("Running scheduled notification scan...");
        List<MedicalProfile> allProfiles = profileRepo.findAll();
        int total = 0;
        for (MedicalProfile p : allProfiles) {
            List<Notification> generated = checkAndNotify(p.getId());
            total += generated.size();
        }
        log.info("Notification scan complete: {} notifications generated for {} profiles", total, allProfiles.size());
    }

    // ── Health Score (same logic as heatmap) ────────

    private double computeHealthScore(MedicalProfile profile, BiometricEntry latest, List<BiometricEntry> entries) {
        if (latest == null) return 50;
        double score = 100;

        double bmi = latest.getBmi() != null ? latest.getBmi() : 25;
        if (bmi > 30) score -= 20;
        else if (bmi > 27) score -= 12;
        else if (bmi > 25) score -= 5;
        else if (bmi < 18.5) score -= 10;

        if (latest.getSystolic() != null) {
            if (latest.getSystolic() > 140) score -= 18;
            else if (latest.getSystolic() > 130) score -= 10;
        }

        if (latest.getGlucose() != null) {
            if (latest.getGlucose() > 126) score -= 18;
            else if (latest.getGlucose() > 100) score -= 8;
        }

        if (latest.getBodyFat() != null && latest.getBodyFat() > 35) score -= 10;

        if (profile.getConditions() != null)
            score -= Math.min(profile.getConditions().size() * 6, 24);

        if (entries.size() >= 2) {
            BiometricEntry prev = entries.get(entries.size() - 2);
            double wd = latest.getWeight() - prev.getWeight();
            if (wd > 2) score -= 8;
            else if (wd < -1) score += 3;
        }

        return Math.max(0, Math.min(100, score));
    }

    // ── Dedup helper — don't create same notification twice in 24h ──

    private Notification createIfNotExists(MedicalProfile profile, String type, String severity,
                                           String title, String message, String icon,
                                           String actionUrl, String actionLabel) {
        boolean exists = notifRepo.existsByProfileIdAndTypeAndTitleAndCreatedAtAfter(
                profile.getId(), type, title, LocalDateTime.now().minusHours(24));
        if (exists) return null;

        Notification n = Notification.builder()
                .profile(profile)
                .type(type)
                .severity(severity)
                .title(title)
                .message(message)
                .icon(icon)
                .actionUrl(actionUrl)
                .actionLabel(actionLabel)
                .build();
        return notifRepo.save(n);
    }
}
