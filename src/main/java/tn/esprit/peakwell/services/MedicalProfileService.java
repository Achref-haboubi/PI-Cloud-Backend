package tn.esprit.peakwell.services;


import tn.esprit.peakwell.dto.MedicalProfileRequest;
import tn.esprit.peakwell.dto.MedicalProfileResponse;
import tn.esprit.peakwell.entities.Dietitian;
import tn.esprit.peakwell.entities.MedicalProfile;
import tn.esprit.peakwell.entities.Student;
import tn.esprit.peakwell.repositories.DietitianRepository;
import tn.esprit.peakwell.repositories.MedicalProfileRepository;
import tn.esprit.peakwell.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalProfileService {

    private final MedicalProfileRepository repository;
    private final StudentRepository studentRepository;
    private final DietitianRepository dietitianRepository;

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

        if (request.getStudentId() != null) {
            studentRepository.findById(request.getStudentId()).ifPresent(profile::setStudent);
        }
        if (request.getDietitianId() != null) {
            dietitianRepository.findById(request.getDietitianId()).ifPresent(profile::setAssignedDietitian);
        }

        boolean complete = request.getFirstName() != null && !request.getFirstName().isBlank()
                && request.getLastName() != null && !request.getLastName().isBlank()
                && request.getDateOfBirth() != null && !request.getDateOfBirth().isBlank()
                && request.getGender() != null && !request.getGender().isBlank()
                && request.getBloodType() != null && !request.getBloodType().isBlank()
                && request.getHeight() != null && request.getHeight() > 0;

        profile.setComplete(complete);

        return toResponse(repository.save(profile));
    }

    public List<MedicalProfileResponse> getAllProfiles() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public MedicalProfileResponse getProfileByStudent(Long studentId) {
        return repository.findByStudentId(studentId).map(this::toResponse).orElse(null);
    }

    public List<MedicalProfileResponse> getProfilesByDietitian(Long dietitianId) {
        return repository.findByAssignedDietitianId(dietitianId)
                .stream().map(this::toResponse).toList();
    }

    public MedicalProfileResponse assignDietitian(Long profileId, Long dietitianId) {
        MedicalProfile profile = repository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        dietitianRepository.findById(dietitianId).ifPresent(profile::setAssignedDietitian);
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
        if (p.getStudent() != null) {
            res.setStudentId(p.getStudent().getId());
            res.setStudentName(p.getStudent().getUser() != null
                ? p.getStudent().getUser().getFirstName() + " " + p.getStudent().getUser().getLastName()
                : null);
        }
        if (p.getAssignedDietitian() != null) {
            res.setDietitianId(p.getAssignedDietitian().getId());
            res.setDietitianSpecialization(p.getAssignedDietitian().getSpecialization());
            res.setDietitianName(p.getAssignedDietitian().getUser() != null
                ? p.getAssignedDietitian().getUser().getFirstName() + " " + p.getAssignedDietitian().getUser().getLastName()
                : null);
        }
        return res;
    }
}
