package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.User;

public interface IRestaurantService  {

    void createRestaurant(User user, ProfileRequest request);
    
}
