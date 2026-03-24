package tn.esprit.peakwell.services;

import org.springframework.stereotype.Service;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.User;

@Service
public interface IDietitianService {
    void createDietitian(User user, ProfileRequest request);
}
