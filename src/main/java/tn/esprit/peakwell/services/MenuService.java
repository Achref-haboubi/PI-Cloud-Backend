package tn.esprit.peakwell.services;

import tn.esprit.peakwell.entities.*;
import tn.esprit.peakwell.repositories.*;
import tn.esprit.peakwell.dto.*;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;

@Service
public class MenuService {

    private final DailyMenuRepository menuRepository;
    private final MealRepository mealRepository;

    public MenuService(DailyMenuRepository menuRepository, MealRepository mealRepository) {
        this.menuRepository = menuRepository;
        this.mealRepository = mealRepository;
    }

    // CREATE MANUAL
    public DailyMenuDTO createMenu(DailyMenuRequest request) {

        DailyMenu menu = new DailyMenu();

        menu.setDate(request.getDate());

        Meal breakfast = getMeal(request.getBreakfastId());
        Meal lunch = getMeal(request.getLunchId());
        Meal dinner = getMeal(request.getDinnerId());

        validateCategory(breakfast, "breakfast");
        validateCategory(lunch, "lunch");
        validateCategory(dinner, "dinner");

        menu.setBreakfast(breakfast);
        menu.setLunch(lunch);
        menu.setDinner(dinner);

        DailyMenu saved = menuRepository.save(menu);

        return mapToDTO(saved);
    }

    private void validateCategory(Meal meal, String expectedCategory) {
        if (!meal.getCategory().equalsIgnoreCase(expectedCategory)) {
            throw new RuntimeException(
                "Invalid meal category: expected " + expectedCategory +
                " but got " + meal.getCategory()
            );
        }
    }

    // AUTO GENERATE MENU
    @Transactional
    public List<DailyMenuDTO> generateWeeklyMenu(LocalDate startOfWeek) {

        List<DailyMenuDTO> weekMenus = new ArrayList<>();

        List<Meal> breakfasts = mealRepository.findByCategoryIgnoreCase("breakfast");
        List<Meal> lunches = mealRepository.findByCategoryIgnoreCase("lunch");
        List<Meal> dinners = mealRepository.findByCategoryIgnoreCase("dinner");

        if (breakfasts.isEmpty() || lunches.isEmpty() || dinners.isEmpty()) {
            throw new RuntimeException("Each category must have at least one meal");
        }

        Collections.shuffle(breakfasts);
        Collections.shuffle(lunches);
        Collections.shuffle(dinners);

        for (int i = 0; i < 7; i++) {

            LocalDate date = startOfWeek.plusDays(i);

            // ✅ éviter duplication
            if (menuRepository.findByDate(date).isPresent()) {
                continue;
            }

            DailyMenu menu = new DailyMenu();
            menu.setDate(date);

            menu.setBreakfast(breakfasts.get(i % breakfasts.size()));
            menu.setLunch(lunches.get(i % lunches.size()));
            menu.setDinner(dinners.get(i % dinners.size()));

            DailyMenu saved = menuRepository.save(menu);
            weekMenus.add(mapToDTO(saved));
        }

        return weekMenus;
    }

    public void generateCurrentWeek() {
        LocalDate start = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        generateWeeklyMenu(start);
    }

    public void generateNextWeek() {
        LocalDate nextWeek = LocalDate.now()
                .with(java.time.DayOfWeek.MONDAY)
                .plusWeeks(1);

        generateWeeklyMenu(nextWeek);
    }

    // GET TODAY
    public DailyMenuDTO getTodayMenu() {
        DailyMenu menu = menuRepository.findByDate(LocalDate.now())
                .orElseThrow(() -> new RuntimeException("Menu not found for today"));

        return mapToDTO(menu);
    }

    // GET ALL
    public List<DailyMenuDTO> getAllMenus() {
        return menuRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // GET WEEK
    public List<DailyMenuDTO> getWeeklyMenus() {

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);

        return menuRepository.findByDateBetween(startOfWeek, endOfWeek)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // GET BY DATE
    public DailyMenuDTO getMenuByDate(LocalDate date) {
        DailyMenu menu = menuRepository.findByDate(date)
                .orElseThrow(() -> new RuntimeException("Menu not found for this date"));

        return mapToDTO(menu);
    }

    // récupérer meal
    private Meal getMeal(Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found"));
    }

    // MAPPER MENU
    private DailyMenuDTO mapToDTO(DailyMenu menu) {
        return new DailyMenuDTO(
                menu.getDate(),
                mapMealToDTO(menu.getBreakfast()),
                mapMealToDTO(menu.getLunch()),
                mapMealToDTO(menu.getDinner())
        );
    }

    public void deleteMenu(Long id) {

        DailyMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        menuRepository.delete(menu);
    }

    // MAPPER MEAL
    private MealDTO mapMealToDTO(Meal meal) {

        if (meal == null) return null;

        MealDTO dto = new MealDTO();

        dto.setId(meal.getId());
        dto.setName(meal.getName());
        dto.setCategory(meal.getCategory());

        dto.setTotalCalories(meal.getTotalCalories());
        dto.setTotalProtein(meal.getTotalProtein());
        dto.setTotalCarbs(meal.getTotalCarbs());
        dto.setTotalFats(meal.getTotalFats());

        dto.setTags(meal.getTags());

        dto.setIngredients(
            meal.getIngredients()
                .stream()
                .map(ing -> new IngredientDTO(
                    ing.getProduct().getName(),
                    ing.getQuantity()
                ))
                .toList()
        );

        dto.setImage(meal.getImage());

        return dto;
    }
}