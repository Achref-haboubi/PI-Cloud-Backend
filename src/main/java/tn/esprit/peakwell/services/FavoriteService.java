package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.peakwell.entities.Favorite;
import tn.esprit.peakwell.entities.Meal;
import tn.esprit.peakwell.repositories.FavoriteRepository;
import tn.esprit.peakwell.repositories.MealRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MealRepository mealRepository;

    public void toggleFavorite(Long mealId) {

        Optional<Favorite> existing =
                favoriteRepository.findByMealId(mealId);

        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());

            // decrement
            meal.setFavoriteCount(Math.max(0, meal.getFavoriteCount() - 1));

        } else {
            Favorite fav = new Favorite();
            fav.setMeal(meal);

            favoriteRepository.save(fav);

            // increment
            meal.setFavoriteCount(meal.getFavoriteCount() + 1);
        }

        mealRepository.save(meal);
    }

    public List<Long> getFavoriteMealIds() {
        return favoriteRepository.findAll()
                .stream()
                .map(f -> f.getMeal().getId())
                .toList();
    }
}