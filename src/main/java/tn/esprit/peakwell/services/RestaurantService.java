package tn.esprit.peakwell.services;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.Restaurant;
import tn.esprit.peakwell.entities.User;

@Service
public class RestaurantService implements IRestaurantService {
    
    @Override
    public void createRestaurant(User user, ProfileRequest request) {

        Restaurant restaurant = user.getRestaurant();

        if (restaurant == null) {
            restaurant = new Restaurant();
            restaurant.setUser(user);
        }

        restaurant.setName(request.getName());
        restaurant.setNumTeleph(request.getNumTeleph());
        restaurant.setAddress(request.getAddress());

        restaurant.setProfileCompleted(true);

        user.setRestaurant(restaurant);
    }
}


