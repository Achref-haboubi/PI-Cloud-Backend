package tn.esprit.peakwell.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import tn.esprit.peakwell.dto.DietitianProfile;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.dto.UpdateProfileRequest;
import tn.esprit.peakwell.entities.Dietitian;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.DietitianRepository;

import tn.esprit.peakwell.services.DietitianService;

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
        dietitian.setCertification(request.getCertification()); // 🔗 certificate URL
        dietitian.setLinkUrl(request.getLinkUrl());
        dietitian.setExperienceYears(request.getExperienceYears());
        dietitian.setConsultationPrice(request.getConsultationPrice());

        user.setDietitian(dietitian);
    }

    @Override
public void updateDietitianProfile(User user, UpdateProfileRequest request) {

    Dietitian dietitian = user.getDietitian();

    if (dietitian == null) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Dietitian profile not found");
    }

    //  Update ONLY dietitian fields

    if (request.getSpecialization() != null) {
        dietitian.setSpecialization(request.getSpecialization());
    }

    if (request.getCertification() != null) {
        dietitian.setCertification(request.getCertification());
    }

    if (request.getLinkUrl() != null) {
        dietitian.setLinkUrl(request.getLinkUrl());
    }

    if (request.getExperienceYears() != null) {
        if (request.getExperienceYears() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid experience years");
        }
        dietitian.setExperienceYears(request.getExperienceYears());
    }

    if (request.getConsultationPrice() != null) {
        if (request.getConsultationPrice() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid consultation price");
        }
        dietitian.setConsultationPrice(request.getConsultationPrice());
    }

}


        @Override
    public DietitianProfile getDietitianProfile(User user) {

        Dietitian dietitian = user.getDietitian();

        if (dietitian == null) {
            return null;
        }

        DietitianProfile dp = new DietitianProfile();
        dp.setSpecialization(dietitian.getSpecialization());
        dp.setExperienceYears(dietitian.getExperienceYears());
        dp.setConsultationPrice(dietitian.getConsultationPrice());
        dp.setLinkUrl(dietitian.getLinkUrl());
        dp.setCertificateUrl(dietitian.getCertification());

        return dp;
    }

}
