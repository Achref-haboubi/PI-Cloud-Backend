package tn.esprit.peakwell.services;


import tn.esprit.peakwell.dto.MedicalProfileRequest;
import tn.esprit.peakwell.dto.MedicalProfileResponse;
import tn.esprit.peakwell.entities.MedicalProfile;
import tn.esprit.peakwell.repositories.MedicalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MedicalProfileService {

    private final MedicalProfileRepository repository;

    // We use id=1 as the single profile for now
    private static final Long PROFILE_ID = 1L;

    public MedicalProfileResponse getProfile() {
        return repository.findById(PROFILE_ID)
                .map(this::toResponse)
                .orElse(null);
    }

    public MedicalProfileResponse saveProfile(MedicalProfileRequest request) {
        MedicalProfile profile = repository.findById(PROFILE_ID)
                .orElse(MedicalProfile.builder().build());

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setBloodType(request.getBloodType());
        profile.setHeight(request.getHeight());
        profile.setEmergencyContact(request.getEmergencyContact());
        profile.setAllergies(request.getAllergies() != null ? request.getAllergies() : new ArrayList<>());
        profile.setConditions(request.getConditions() != null ? request.getConditions() : new ArrayList<>());
        profile.setMedications(request.getMedications() != null ? request.getMedications() : new ArrayList<>());

        boolean complete = request.getFirstName() != null && !request.getFirstName().isBlank()
                && request.getLastName() != null && !request.getLastName().isBlank()
                && request.getDateOfBirth() != null && !request.getDateOfBirth().isBlank()
                && request.getGender() != null && !request.getGender().isBlank()
                && request.getBloodType() != null && !request.getBloodType().isBlank()
                && request.getHeight() != null && request.getHeight() > 0;

        profile.setComplete(complete);

        return toResponse(repository.save(profile));
    }

    private MedicalProfileResponse toResponse(MedicalProfile p) {
        MedicalProfileResponse res = new MedicalProfileResponse();
        res.setId(p.getId());
        res.setFirstName(p.getFirstName());
        res.setLastName(p.getLastName());
        res.setDateOfBirth(p.getDateOfBirth());
        res.setGender(p.getGender());
        res.setBloodType(p.getBloodType());
        res.setHeight(p.getHeight());
        res.setEmergencyContact(p.getEmergencyContact());
        res.setAllergies(p.getAllergies());
        res.setConditions(p.getConditions());
        res.setMedications(p.getMedications());
        res.setComplete(p.getComplete());
        return res;
    }
}
