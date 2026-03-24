package tn.esprit.peakwell.services;

import org.springframework.stereotype.Service;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.Dietitian;
import tn.esprit.peakwell.entities.User;

@Service
public class DietitianService implements IDietitianService{

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
}
