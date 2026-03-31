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
        dietitian.setCertification(request.getCertification()); // 🔗 certificate URL
        dietitian.setLinkUrl(request.getLinkUrl());
        dietitian.setImgUrl(request.getImgUrl());
        dietitian.setExperienceYears(request.getExperienceYears());
        dietitian.setConsultationPrice(request.getConsultationPrice());

        dietitian.setProfileCompleted(true);

        user.setDietitian(dietitian);
    }

    @Override
    public Dietitian setDietitianActiveStatus(Long id, boolean active) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDietitianActiveStatus'");
    }

    @Override
    public void updateDietitian(User user, ProfileRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateDietitian'");
    }
    

    // @Override
    // public Dietitian setDietitianActiveStatus(Long id, boolean active) {

    //     Dietitian dietitian = dietitianRepository.findById(id)
    //             .orElseThrow(() ->
    //                     new ResponseStatusException(HttpStatus.NOT_FOUND, "Dietitian not found")
    //             );

    //     if (dietitian.isActive() == active) {
    //         throw new ResponseStatusException(
    //                 HttpStatus.BAD_REQUEST,
    //                 active ? "Dietitian already active" : "Dietitian already inactive"
    //         );
    //     }

    //     dietitian.setActive(active);

    //     return dietitianRepository.save(dietitian);
    // }

    // @Override
    // public void updateDietitian(User user, ProfileRequest request) {

    //     try {

    //         Dietitian dietitian = user.getDietitian();

    //         if (dietitian == null) {
    //             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dietitian profile not found");
    //         }

    //         if (request.getConsultationPrice() != null && request.getConsultationPrice() < 0) {
    //             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid price");
    //         }

    //         if (request.getSpecialization() != null) {
    //             dietitian.setSpecialization(request.getSpecialization());
    //         }

    //         if (request.getCertification() != null) {
    //             dietitian.setCertification(request.getCertification());
    //         }

    //         if (request.getLinkUrl() != null) {
    //             dietitian.setLinkUrl(request.getLinkUrl());
    //         }

    //         if (request.getImgUrl() != null) {
    //             dietitian.setImgUrl(request.getImgUrl());
    //         }

    //         if (request.getExperienceYears() != null) {
    //             dietitian.setExperienceYears(request.getExperienceYears());
    //         }

    //         if (request.getConsultationPrice() != null) {
    //             dietitian.setConsultationPrice(request.getConsultationPrice());
    //         }

    //         //  optional: re-approval
    //         dietitian.setActive(false);

    //     } catch (ResponseStatusException ex) {
    //         throw ex;

    //     } catch (Exception ex) {
    //         throw new ResponseStatusException(
    //                 HttpStatus.INTERNAL_SERVER_ERROR,
    //                 "Internal server error"
    //         );
    //     }
    // }
}
