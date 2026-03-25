package tn.esprit.peakwell.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.Dietitian;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.DietitianRepository;

@Service
public class DietitianService implements IDietitianService{

    @Autowired
    DietitianRepository dietitianRepository;

    @Override
    public void createDietitian(User user, ProfileRequest request) {

        Dietitian dietitian = user.getDietitian();

        if (dietitian == null) {
            dietitian = new Dietitian();
            dietitian.setUser(user);
        }

        dietitian.setSpecialization(request.getSpecialization());
        dietitian.setCertification(request.getCertification());
        dietitian.setLinkUrl(request.getLinkUrl());
        dietitian.setImgUrl(request.getImgUrl());
        dietitian.setExperienceYears(request.getExperienceYears());
        dietitian.setConsultationPrice(request.getConsultationPrice());


        user.setDietitian(dietitian);
    }

    @Override
    public Dietitian setDietitianActiveStatus(Long id, boolean active) {

        Dietitian dietitian = dietitianRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Dietitian not found")
                );

        if (dietitian.isActive() == active) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    active ? "Dietitian already active" : "Dietitian already inactive"
            );
        }

        dietitian.setActive(active);

        return dietitianRepository.save(dietitian);
    }
}
