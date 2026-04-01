package tn.esprit.peakwell.services;

import org.springframework.stereotype.Service;

import tn.esprit.peakwell.dto.DietitianProfile;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.Dietitian;
import tn.esprit.peakwell.entities.User;

@Service
public interface IDietitianService {
    void createDietitian(User user, ProfileRequest request);
    Dietitian setDietitianActiveStatus(Long id, boolean active);
    void updateDietitian(User user, ProfileRequest request);
    DietitianProfile getDietitianProfile(User user);
}
