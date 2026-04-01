package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.peakwell.dto.ProfileRequest;
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
    public User updateProfile(ProfileRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateProfile'");
    }

    // @Override
    // public User updateProfile(ProfileRequest request) {

    //     try {

    //         String keycloakId = authService.getCurrentUserId(); //  may throw 401

    //         User user = userRepository.findByKeycloakId(keycloakId)
    //                 .orElseThrow(() ->
    //                         new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    //                 );

    //         //  profile not completed
    //         if (!user.isProfileCompleted()) {
    //             throw new ResponseStatusException(
    //                     HttpStatus.BAD_REQUEST,
    //                     "Complete profile first"
    //             );
    //         }

    //         //  block inactive dietitian
    //         if (user.getDietitian() != null && Boolean.FALSE.equals(user.getDietitian().isActive())) {
    //             throw new ResponseStatusException(
    //                     HttpStatus.FORBIDDEN,
    //                     "Account pending admin approval"
    //             );
    //         }

    //         //  role-based update
    //         if (user.getStudent() != null) {
    //             studentService.updateStudent(user, request);
    //         } else if (user.getDietitian() != null) {
    //             dietitianService.updateDietitian(user, request);
    //         } else {
    //             throw new ResponseStatusException(
    //                     HttpStatus.BAD_REQUEST,
    //                     "No profile found"
    //             );
    //         }

    //         return userRepository.save(user);

    //     } catch (ResponseStatusException ex) {
    //         throw ex; // keep business errors

    //     } catch (Exception ex) {
    //         throw new ResponseStatusException(
    //                 HttpStatus.INTERNAL_SERVER_ERROR,
    //                 "Internal server error"
    //         );
    //     }
    // }


}
