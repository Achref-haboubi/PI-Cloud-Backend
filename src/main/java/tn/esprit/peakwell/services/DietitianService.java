package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.DietitianProfileRequest;
import tn.esprit.peakwell.entities.Role;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.entities.Dietitian;
import tn.esprit.peakwell.repositories.DietitianRepository;
import tn.esprit.peakwell.repositories.userRepository;
import tn.esprit.peakwell.security.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DietitianService implements IDietitianService {
    @Autowired
    DietitianRepository dietitianRepository;
    userRepository userRepository;
    private final JwtUtils jwtUtils;


    @Override
    public Dietitian completeDietitianProfile(String token, DietitianProfileRequest request) {

        //  Extract userId
        Long userId = jwtUtils.extractUserId(token);

        //  Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Check role
        if (user.getRole() != Role.DIETITIAN) {
            throw new RuntimeException("Access denied: not a dietitian");
        }

        //  Prevent duplicate
        if (dietitianRepository.existsById(userId)) {
            throw new RuntimeException("Dietitian profile already exists");
        }

        //  Create profile
        Dietitian dietitian = new Dietitian();
        dietitian.setUser(user);

        dietitian.setSpecialization(request.getSpecialization());
        dietitian.setCertification(request.getCertification());
        dietitian.setLinkUrl(request.getLinkUrl());
        dietitian.setExperienceYears(request.getExperienceYears());
        dietitian.setConsultationPrice(request.getConsultationPrice());

        //  Save
        Dietitian savedDietitian = dietitianRepository.save(dietitian);

        //  Update user
        user.setProfileCompleted(true);
        userRepository.save(user);

        //  Return created profile
        return savedDietitian;
    }
}
