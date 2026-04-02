package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.dto.UpdateProfileRequest;
import tn.esprit.peakwell.dto.UserProfile;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

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

        //  Upload files using NEW method (with folder structure)
        String imageUrl = fileUploadService.uploadFile(image, role, "profile");
        String certificateUrl = fileUploadService.uploadFile(certificate, role, "certificate");

        if ("STUDENT".equals(role)) {

            if (user.getStudent() != null && user.isProfileCompleted()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Student profile already completed");
            }

            //  Student needs profile image
            if (imageUrl == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Profile image is required");
            }

            request.setImgUrl(imageUrl);

            studentService.createStudent(user, request);

        } else if ("DIETITIAN".equals(role)) {

            if (user.getDietitian() != null && user.isProfileCompleted()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Dietitian profile already completed");
            }

            //  Dietitian needs BOTH
            if (imageUrl == null || certificateUrl == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Profile image and certificate are required");
            }

            request.setImgUrl(imageUrl);
            request.setCertification(certificateUrl);

            dietitianService.createDietitian(user, request);

            //  disable account until admin validation
            user.setEnabled(false);
            user.setProfileCompleted(true);

        }  else {
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

        // inject into request (like completeProfile)
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
}