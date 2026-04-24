package tn.esprit.peakwell.services;

import tn.esprit.peakwell.entities.*;
import tn.esprit.peakwell.exception.StockException;
import tn.esprit.peakwell.repositories.MealRepository;
import tn.esprit.peakwell.repositories.ProductRepository;
import tn.esprit.peakwell.dto.MealDTO;
import tn.esprit.peakwell.dto.IngredientDTO;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final ProductRepository productRepository;

    public MealService(MealRepository mealRepository, ProductRepository productRepository) {
        this.mealRepository = mealRepository;
        this.productRepository = productRepository;
    }

    // ✅ CREATE
    public Meal createMeal(Meal meal) {

        for (Ingredient ing : meal.getIngredients()) {

            Product product = productRepository.findById(ing.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found with id: " + ing.getProduct().getId()));

            double quantity = ing.getQuantity();

            // ❌ Vérifier stock
            if (product.getStock() < quantity) {
                throw new StockException("Not enough stock for product: " + product.getName());
            }

            // 🔥 Décrémenter stock
            product.setStock(product.getStock() - quantity);

            // 🔗 Lier relations
            ing.setProduct(product);
            ing.setMeal(meal);
        }

        calculateNutrition(meal);

        return mealRepository.save(meal);
    }

    // 🧠 CALCUL
    private void calculateNutrition(Meal meal) {

        double calories = 0, protein = 0, carbs = 0, fats = 0;

        for (Ingredient ing : meal.getIngredients()) {

            Product p = ing.getProduct();
            double q = ing.getQuantity();

            calories += (p.getCalories() * q) / 100;
            protein  += (p.getProtein() * q) / 100;
            carbs    += (p.getCarbs() * q) / 100;
            fats     += (p.getFats() * q) / 100;
        }

        meal.setTotalCalories(calories);
        meal.setTotalProtein(protein);
        meal.setTotalCarbs(carbs);
        meal.setTotalFats(fats);

        meal.setTags(generateTags(calories, protein, carbs, fats));    }

    // 🏷️ TAGS
    private String generateTags(double calories, double protein, double carbs, double fats) {

        // 💪 High protein
        if (protein >= 30) return "high-protein";

        // 🍞 High carbs
        if (carbs >= 50) return "high-carb";

        // 🧈 Low fat
        if (fats <= 10) return "low-fat";

        // 🔥 Low calorie
        if (calories <= 400) return "low-calorie";

        // 🥑 Keto (low carbs high fat)
        if (carbs < 20 && fats > 20) return "low-carbs-high-fat";

        return "balanced";
    }

    // 🔁 MAPPER Ingredient → DTO
    private IngredientDTO mapIngredientToDTO(Ingredient ing) {
        return new IngredientDTO(
                ing.getProduct().getName(),
                ing.getQuantity()
        );
    }

    // 🔁 MAPPER Meal → DTO
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
                        .map(this::mapIngredientToDTO)
                        .toList()
        );
    }

    // ✅ GET ALL → DTO
    public List<MealDTO> getAllMeals() {
        return mealRepository.findAll()
                .stream()
                .map(this::mapMealToDTO)
                .toList();
    }

    // ✅ GET BY ID → DTO
    public MealDTO getMeal(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        return mapMealToDTO(meal);
    }

    public List<MealDTO> getMealsByCategory(String category) {
        return mealRepository.findByCategory(category)
            .stream()
            .map(this::mapMealToDTO)
            .toList();
}

    public List<MealDTO> getMealsByTags(String tag) {
        return mealRepository.findByTags(tag)
            .stream()
            .map(this::mapMealToDTO)
            .toList();
}
}