package tn.esprit.peakwell.services;

import tn.esprit.peakwell.entities.*;
import tn.esprit.peakwell.exception.StockException;
import tn.esprit.peakwell.repositories.MealRepository;
import tn.esprit.peakwell.repositories.ProductRepository;
import tn.esprit.peakwell.dto.MealDTO;
import tn.esprit.peakwell.dto.MealRequest;
import tn.esprit.peakwell.dto.IngredientDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final ProductRepository productRepository;
    private final AIAlergeneService aiAlergeneService;

    public MealService(MealRepository mealRepository,
                       ProductRepository productRepository,
                       AIAlergeneService aiAlergeneService) {
        this.mealRepository = mealRepository;
        this.productRepository = productRepository;
        this.aiAlergeneService = aiAlergeneService;
    }

    // CREATE 
    public MealDTO createMeal(MealRequest request) {

        Meal meal = new Meal();
        meal.setName(request.getName());
        meal.setCategory(request.getCategory());

        List<Ingredient> ingredients = request.getIngredients()
                .stream()
                .map(req -> {

                    Product product = productRepository.findById(req.getProductId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Product not found with id: " + req.getProductId()));

                    if (product.getStock() < req.getQuantity()) {
                        throw new StockException("Not enough stock for product: " + product.getName());
                    }

                    product.setStock(product.getStock() - req.getQuantity());

                    Ingredient ing = new Ingredient();
                    ing.setProduct(product);
                    ing.setQuantity(req.getQuantity());
                    ing.setMeal(meal);

                    return ing;
                })
                .collect(Collectors.toList()); 

        meal.setIngredients(new ArrayList<>(ingredients)); 

        calculateNutrition(meal);

        String text = buildMealText(meal);
        var prediction = aiAlergeneService.predictAllergens(text);
        meal.setPredictedAllergens(prediction.getPredictedAllergens());

        Meal saved = mealRepository.save(meal);

        return mapMealToDTO(saved);
    }

    // UPDATE
    @Transactional
    public MealDTO updateMeal(Long id, MealRequest request) {

        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        // RESTORE STOCK
        for (Ingredient ing : meal.getIngredients()) {
            Product product = ing.getProduct();
            product.setStock(product.getStock() + ing.getQuantity());
        }

        // CLEAR
        meal.getIngredients().clear();

        meal.setName(request.getName());
        meal.setCategory(request.getCategory());

        // ADD NEW INGREDIENTS
        for (var req : request.getIngredients()) {

            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStock() < req.getQuantity()) {
                throw new StockException("Not enough stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - req.getQuantity());

            Ingredient ing = new Ingredient();
            ing.setProduct(product);
            ing.setQuantity(req.getQuantity());
            ing.setMeal(meal);
            meal.getIngredients().add(ing);
        }

        calculateNutrition(meal);

        return mapMealToDTO(mealRepository.save(meal));
    }

    public void deleteMeal(Long id) {

        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        for (Ingredient ing : meal.getIngredients()) {
            Product product = ing.getProduct();
            product.setStock(product.getStock() + ing.getQuantity());
        }

        mealRepository.delete(meal);
    }

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

        meal.setTags(generateTags(calories, protein, carbs, fats));
    }

    private String generateTags(double calories, double protein, double carbs, double fats) {

        if (protein >= 30) return "high-protein";
        if (carbs >= 50) return "high-carb";
        if (fats <= 10) return "low-fat";
        if (calories <= 400) return "low-calorie";
        if (carbs < 20 && fats > 20) return "low-carbs-high-fat";

        return "balanced";
    }

    private IngredientDTO mapIngredientToDTO(Ingredient ing) {
        return new IngredientDTO(
                ing.getProduct().getName(),
                ing.getQuantity()
        );
    }

    private MealDTO mapMealToDTO(Meal meal) {

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
                .map(this::mapIngredientToDTO)
                .collect(Collectors.toList())
        );

        dto.setImage(meal.getImage());

        dto.setPredictedAllergens(meal.getPredictedAllergens());
        dto.setFavoriteCount(meal.getFavoriteCount());

        return dto;
    }

    public List<MealDTO> getAllMeals() {
        return mealRepository.findAll()
                .stream()
                .map(this::mapMealToDTO)
                .collect(Collectors.toList()); 
    }

    public MealDTO getMeal(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        return mapMealToDTO(meal);
    }

    public List<MealDTO> getMealsByCategory(String category) {
        return mealRepository.findByCategory(category)
                .stream()
                .map(this::mapMealToDTO)
                .collect(Collectors.toList()); 
    }

    public List<MealDTO> getMealsByTags(String tag) {
        return mealRepository.findByTags(tag)
                .stream()
                .map(this::mapMealToDTO)
                .collect(Collectors.toList()); 
    }

    private String buildMealText(Meal meal) {
        StringBuilder text = new StringBuilder();

        text.append(meal.getName()).append(" ");

        meal.getIngredients()
                .stream()
                .map(ing -> ing.getProduct().getName())
                .distinct()
                .forEach(name -> text.append(name).append(" "));

        return text.toString().trim();
    }

    public void attachImage(Long mealId, String fileName) {
        Meal meal = mealRepository.findById(mealId)
            .orElseThrow(() -> new RuntimeException("Meal not found"));

        meal.setImage(fileName);
        mealRepository.save(meal);
    }

    

}