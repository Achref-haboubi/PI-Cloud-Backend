package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.entities.Meal;
import tn.esprit.peakwell.dto.MealDTO;
import tn.esprit.peakwell.services.MealService;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/meals")
@CrossOrigin("*")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    // ✅ CREATE (reste avec Entity)
    @PostMapping
    public Meal createMeal(@Valid @RequestBody Meal meal) {
        return mealService.createMeal(meal);
    }

    // ✅ GET ALL → DTO
    @GetMapping
    public List<MealDTO> getAllMeals() {
        return mealService.getAllMeals();
    }

    // ✅ GET BY ID → DTO
    @GetMapping("/{id}")
    public MealDTO getMeal(@PathVariable Long id) {
        return mealService.getMeal(id);
    }

    @GetMapping("/category/{cat}")
    public List<MealDTO> getByCategory(@PathVariable String cat) {
        return mealService.getMealsByCategory(cat);
    }

    @GetMapping("/tag/{tag}")
    public List<MealDTO> getByTag(@PathVariable String tag) {
        return mealService.getMealsByTags(tag);
    }
}