package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.DailyPlanDTO;
import tn.esprit.peakwell.entities.*;
import tn.esprit.peakwell.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class PlanService {

    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private DailyMenuRepository dailyMenuRepository;

    private static final List<String> USER_ALLERGIES = List.of("LACTOSE");

    // =====================================================

    public DailyPlanDTO generateTodayPlan() {

        // 🔐 1. user connecté
        Jwt jwt = (Jwt) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String email = jwt.getClaim("email");

        if (email == null) {
            email = jwt.getClaim("preferred_username");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // 👤 2. student
        Student student = studentRepository.findByUser(user);
        if (student == null) {
            throw new RuntimeException("Student not found");
        }

        // 🍽️ 3. menu
        Optional<DailyMenu> menuOpt = dailyMenuRepository.findByDate(LocalDate.now());

        if (menuOpt.isEmpty()) {
            throw new RuntimeException("No menu available");
        }

        DailyMenu menu = menuOpt.get();

        // ⚠️ IMPORTANT : adapte selon TON entity
        List<Meal> meals = List.of(
            menu.getBreakfast(),
            menu.getLunch(),
            menu.getDinner()
        );

        // 🧠 4. validation
        if (isMenuValid(meals, student)) {
            return buildPlan(meals, "MENU_VALID");
        }

        // 🚫 5. filtrage allergie
        List<Meal> safeMeals = meals.stream()
                .filter(Objects::nonNull)
                .filter(this::isSafe)
                .toList();

        if (safeMeals.isEmpty()) {
            throw new RuntimeException("No safe meals available");
        }

        // 🎯 6. génération
        return generateSmartPlan(student, safeMeals);
    }

    // =====================================================
    // VALIDATION MENU
    // =====================================================

    private boolean isMenuValid(List<Meal> meals, Student student) {

        boolean allergiesOk = meals.stream()
                .filter(Objects::nonNull)
                .allMatch(this::isSafe);

        double totalCalories = meals.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Meal::getTotalCalories)
                .sum();

        double target = calculateCalories(student);

        boolean goalOk;

        // ⚠️ TON goal est probablement STRING
        if (student.getGoal().equals("LOSE_WEIGHT")) {
            goalOk = totalCalories <= target;
        } else if (student.getGoal().equals("GAIN_WEIGHT")) {
            goalOk = totalCalories >= target;
        } else {
            goalOk = Math.abs(totalCalories - target) < 200;
        }

        return allergiesOk && goalOk;
    }

    // =====================================================
    // ALLERGIES (AI)
    // =====================================================

    private boolean isSafe(Meal meal) {

        List<String> allergens = meal.getPredictedAllergens();

        if (allergens == null || allergens.isEmpty()) {
            return true;
        }

        return allergens.stream()
                .map(String::toUpperCase)
                .noneMatch(USER_ALLERGIES::contains);
    }


    // =====================================================
    // CALORIES
    // =====================================================

    private double calculateCalories(Student student) {

        double calories = student.getWeight() * 30;

        if (student.getGoal().equals("LOSE_WEIGHT")) {
            calories -= 500;
        } else if (student.getGoal().equals("GAIN_WEIGHT")) {
            calories += 500;
        }

        return calories;
    }

    // =====================================================
    // SMART PLAN
    // =====================================================

    private DailyPlanDTO generateSmartPlan(Student student, List<Meal> meals) {

        double target = calculateCalories(student);

        Meal breakfast = selectBest(meals, "BREAKFAST", target * 0.25);
        Meal lunch = selectBest(meals, "LUNCH", target * 0.40);
        Meal dinner = selectBest(meals, "DINNER", target * 0.35);

        return DailyPlanDTO.builder()
                .breakfast(breakfast)
                .lunch(lunch)
                .dinner(dinner)
                .totalCalories(target)
                .status("GENERATED")
                .build();
    }

    // =====================================================
    // SELECTION
    // =====================================================

    private Meal selectBest(List<Meal> meals, String category, double targetCalories) {

        return meals.stream()
                .filter(m -> m != null && m.getCategory().equals(category))
                .min(Comparator.comparing(m ->
                        Math.abs(m.getTotalCalories() - targetCalories)))
                .orElse(null);
    }

    // =====================================================
    // BUILD PLAN
    // =====================================================

    private DailyPlanDTO buildPlan(List<Meal> meals, String status) {

        double totalCalories = meals.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Meal::getTotalCalories)
                .sum();

        return DailyPlanDTO.builder()
                .breakfast(meals.get(0))
                .lunch(meals.get(1))
                .dinner(meals.get(2))
                .totalCalories(totalCalories)
                .status(status)
                .build();
    }
}