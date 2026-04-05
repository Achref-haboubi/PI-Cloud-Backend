package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import tn.esprit.peakwell.dto.AccountStatusUpdateRequest;
import tn.esprit.peakwell.dto.DietitianProfile;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.dto.StudentProfile;
import tn.esprit.peakwell.dto.UpdateProfileRequest;
import tn.esprit.peakwell.dto.UserProfile;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    @Autowired
    private IEmailService emailService
;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final StudentService studentService;
    private final DietitianService dietitianService;
    private final IFileUploadService fileUploadService;
    private final KeycloakService keycloakService;
   
    
   @Override
public void completeProfile(ProfileRequest request, MultipartFile image, MultipartFile certificate) {

    try {

        String keycloakId = authService.getCurrentUserId();

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        String role = request.getRole();

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Role is required");
        }

        //  Validate phone
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Phone number is required");
        }

        //  Validate address
        if (request.getAddress() == null ||
            request.getAddress().getStreet() == null ||
            request.getAddress().getCity() == null ||
            request.getAddress().getPostalCode() == null ||
            request.getAddress().getCountry() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Incomplete address");
        }

        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());

        String imageUrl = fileUploadService.uploadFile(image, role, "profile");
        String certificateUrl = fileUploadService.uploadFile(certificate, role, "certificate");

        if ("STUDENT".equals(role)) {

            if (user.getStudent() != null && user.isProfileCompleted()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Student profile already completed");
            }

            if (imageUrl == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Profile image is required");
            }

            user.setImgUrl(imageUrl);
            user.setProfileCompleted(true);

            request.setImgUrl(imageUrl);

            studentService.createStudent(user, request);

        } else if ("DIETITIAN".equals(role)) {

            if (user.getDietitian() != null && user.isProfileCompleted()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Dietitian profile already completed");
            }

            if (imageUrl == null || certificateUrl == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Profile image and certificate are required");
            }

            user.setImgUrl(imageUrl);
            user.setEnabled(false);
            user.setProfileCompleted(true);

            request.setImgUrl(imageUrl);
            request.setCertification(certificateUrl);

            dietitianService.createDietitian(user, request);

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid role");
        }

        userRepository.save(user);

    } catch (ResponseStatusException ex) {
        throw ex;

    } catch (Exception ex) {
        ex.printStackTrace();

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later."
        );
    }
}

@Override
public void updateProfile(UpdateProfileRequest request, MultipartFile image, MultipartFile certificate) {

    try {

        String keycloakId = authService.getCurrentUserId();

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        String role = user.getRole().name(); // role already exists

       
        boolean nameUpdated = false;

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
            nameUpdated = true;
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
            nameUpdated = true;
        }

        if (nameUpdated) {
            keycloakService.updateUserNames(
                    keycloakId,
                    user.getFirstName(),
                    user.getLastName()
            );
        }

    
        String imageUrl = fileUploadService.uploadFile(image, role, "profile");
        String certificateUrl = fileUploadService.uploadFile(certificate, role, "certificate");

        //  UPDATE USER SHARED FIELDS

if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
    user.setPhoneNumber(request.getPhoneNumber());
}

if (request.getAddress() != null) {
    user.setAddress(request.getAddress());
}

if (request.getImgUrl() != null) {
    user.setImgUrl(request.getImgUrl());
}
        if (imageUrl != null) {
            request.setImgUrl(imageUrl);
        }

        if (certificateUrl != null) {
            request.setCertification(certificateUrl);
        }

        if ("STUDENT".equals(role)) {

            if (user.getStudent() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Student profile not found");
            }

            studentService.updateStudentProfile(user, request);

        } else if ("DIETITIAN".equals(role)) {

            if (user.getDietitian() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Dietitian profile not found");
            }

            dietitianService.updateDietitianProfile(user, request);

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid role");
        }

        userRepository.save(user);

    } catch (ResponseStatusException ex) {
        throw ex;

    } catch (Exception ex) {
        ex.printStackTrace();

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong while updating profile"
        );
    }
}

    @Override
    @Transactional(readOnly = true)
    public UserProfile getCurrentUserProfile() {

        //  Get current user
        String keycloakId = authService.getCurrentUserId();

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Basic mapping
        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setEmail(user.getEmail());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setRole(user.getRole().toString());
        profile.setProfileCompleted(user.isProfileCompleted());
        profile.setEnabled(user.isEnabled());
        profile.setPhoneNumber(user.getPhoneNumber());
        profile.setImageUrl(user.getImgUrl());
        profile.setAddress(user.getAddress());

        //  Delegate to services
        if (user.getRole().toString().equals("STUDENT")) {
            profile.setStudentProfile(studentService.getStudentProfile(user));
        }

        if (user.getRole().toString().equals("DIETITIAN")) {
            profile.setDietitianProfile(dietitianService.getDietitianProfile(user));
        }

        return profile;
    }

    @Override
    public void toggleStatus(Long userId, AccountStatusUpdateRequest request) {

    try {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        user.setEnabled(!user.isEnabled());

       userRepository.save(user);

         String safeMessage = request.getMessage()
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");

    Map<String, Object> variables = Map.of(
            "name", user.getFirstName(),
            "status", user.isEnabled() ? "ACTIVE" : "BANNED",
            "message", safeMessage
    );

    emailService.sendAccountStatusEmail(
            user.getEmail(),
            request.getSubject(),
            "account-status",
            variables
    );

    } catch (ResponseStatusException ex) {
        throw ex;

    } catch (Exception ex) {
        ex.printStackTrace();

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error while updating user status"
        );
    }
}

  @Override
   public List<UserProfile> getAllUsers() {

    List<User> users = userRepository.findAllWithProfiles();

    return users.stream()
            .map(this::mapToUserProfile)
            .toList();
}


private UserProfile mapToUserProfile(User user) {

    UserProfile dto = new UserProfile();

    dto.setId(user.getId());
    dto.setEmail(user.getEmail());
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setRole(user.getRole().name());
    dto.setProfileCompleted(user.isProfileCompleted());

    
    dto.setEnabled(user.isEnabled());
    dto.setPhoneNumber(user.getPhoneNumber());
    dto.setImageUrl(user.getImgUrl());
    dto.setAddress(user.getAddress());

    // Student
    if (user.getStudent() != null) {

        StudentProfile sp = new StudentProfile();

        sp.setWeight(user.getStudent().getWeight() != null
                ? user.getStudent().getWeight().doubleValue()
                : null);

        sp.setHeight(user.getStudent().getHeight() != null
                ? user.getStudent().getHeight().doubleValue()
                : null);

        sp.setActivityLevel(user.getStudent().getActivityLevel());
        sp.setGoal(user.getStudent().getGoal());

        dto.setStudentProfile(sp);
    }

    
    if (user.getDietitian() != null) {

        DietitianProfile dp = new DietitianProfile();

        dp.setSpecialization(user.getDietitian().getSpecialization());
        dp.setExperienceYears(user.getDietitian().getExperienceYears());
        dp.setConsultationPrice(user.getDietitian().getConsultationPrice());
        dp.setLinkUrl(user.getDietitian().getLinkUrl());
        dp.setCertificateUrl(user.getDietitian().getCertification());

        dto.setDietitianProfile(dp);
    }

    return dto;
}


}