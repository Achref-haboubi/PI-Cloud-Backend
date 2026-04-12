package tn.esprit.peakwell.services;

import org.springframework.stereotype.Service;
import tn.esprit.peakwell.dto.DailyMenuDTO;
import tn.esprit.peakwell.dto.IngredientDTO;
import tn.esprit.peakwell.dto.MealDTO;
import tn.esprit.peakwell.entities.DailyMenu;
import tn.esprit.peakwell.entities.Meal;
import tn.esprit.peakwell.repositories.DailyMenuRepository;
import tn.esprit.peakwell.repositories.MealRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class MenuService {

    private final DailyMenuRepository menuRepository;
    private final MealRepository mealRepository;

    public MenuService(DailyMenuRepository menuRepository, MealRepository mealRepository) {
        this.menuRepository = menuRepository;
        this.mealRepository = mealRepository;
    }

    // ✅ CREATE MANUAL (reste avec Entity)
    public DailyMenu createMenu(DailyMenu menu) {

        menu.setBreakfast(getMeal(menu.getBreakfast().getId()));
        menu.setLunch(getMeal(menu.getLunch().getId()));
        menu.setDinner(getMeal(menu.getDinner().getId()));

        return menuRepository.save(menu);
    }

    // 🔥 NEW → AUTO GENERATE MENU (RANDOM)
    public DailyMenu generateMenu() {

        LocalDate today = LocalDate.now();

        // 🔥 récupérer menu existant ou créer nouveau
        DailyMenu menu = menuRepository.findByDate(today)
                .orElse(new DailyMenu());

        // 🔥 récupérer meals (ignore case recommandé)
        List<Meal> breakfasts = mealRepository.findByCategoryIgnoreCase("breakfast");
        List<Meal> lunches = mealRepository.findByCategoryIgnoreCase("lunch");
        List<Meal> dinners = mealRepository.findByCategoryIgnoreCase("dinner");

        if (breakfasts.isEmpty() || lunches.isEmpty() || dinners.isEmpty()) {
            throw new RuntimeException("Not enough meals to generate menu");
        }

        Random random = new Random();

        // 🔥 assigner nouvelle data
        menu.setDate(today);
        menu.setBreakfast(breakfasts.get(random.nextInt(breakfasts.size())));
        menu.setLunch(lunches.get(random.nextInt(lunches.size())));
        menu.setDinner(dinners.get(random.nextInt(dinners.size())));

        if (menu.getId() != null) {
            System.out.println("Menu updated for today");
        } else {
            System.out.println("New menu created");
        }

        return menuRepository.save(menu);
    }

    // 🧠 GET TODAY → DTO
    public DailyMenuDTO getTodayMenu() {
        DailyMenu menu = menuRepository.findByDate(LocalDate.now())
                .orElseThrow(() -> new RuntimeException("Menu not found for today"));

        return mapToDTO(menu);
    }

    // 🧠 GET ALL → DTO
    public List<DailyMenuDTO> getAllMenus() {
        return menuRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // 🧠 GET WEEK → DTO
    public List<DailyMenuDTO> getWeeklyMenus() {

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);

        return menuRepository.findByDateBetween(startOfWeek, endOfWeek)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // 🧠 GET BY DATE → DTO
    public DailyMenuDTO getMenuByDate(LocalDate date) {
        DailyMenu menu = menuRepository.findByDate(date)
                .orElseThrow(() -> new RuntimeException("Menu not found for this date"));

        return mapToDTO(menu);
    }

    // 🔧 récupérer meal
    private Meal getMeal(Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found"));
    }

    // 🔁 MAPPER MENU → DTO
    private DailyMenuDTO mapToDTO(DailyMenu menu) {
        return new DailyMenuDTO(
                menu.getDate(),
                mapMealToDTO(menu.getBreakfast()),
                mapMealToDTO(menu.getLunch()),
                mapMealToDTO(menu.getDinner())
        );
    }

    // 🔁 MAPPER MEAL → DTO
    private MealDTO mapMealToDTO(Meal meal) {
        return new MealDTO(
                meal.getId(),
                meal.getName(),
                meal.getCategory(),
                meal.getTotalCalories(),
                meal.getTotalProtein(),
                meal.getTotalCarbs(),
                meal.getTotalFats(),
                meal.getTags(),
                meal.getIngredients()
                        .stream()
                        .map(ing -> new IngredientDTO(
                                ing.getProduct().getName(),
                                ing.getQuantity()
                        ))
                        .toList()
        );
    }
}