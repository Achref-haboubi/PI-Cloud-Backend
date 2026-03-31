package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.UserRepository;
import java.io.File;


@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final RestaurantService restaurantService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final StudentService studentService;
    private final DietitianService dietitianService;
    private final IFileUploadService fileUploadService;
   

    
   @Override
public void completeProfile(ProfileRequest request,
                            MultipartFile image,
                            MultipartFile certificate) {

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

        // 🔥 Upload files using NEW method (with folder structure)
        String imageUrl = fileUploadService.uploadFile(image, role, "profile");
        String certificateUrl = fileUploadService.uploadFile(certificate, role, "certificate");

        if ("STUDENT".equals(role)) {

            if (user.getStudent() != null && user.getStudent().isProfileCompleted()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Student profile already completed");
            }

            // ✅ Student needs profile image
            if (imageUrl == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Profile image is required");
            }

            request.setImgUrl(imageUrl);

            studentService.createStudent(user, request);

        } else if ("DIETITIAN".equals(role)) {

            if (user.getDietitian() != null && user.getDietitian().isProfileCompleted()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Dietitian profile already completed");
            }

            // ✅ Dietitian needs BOTH
            if (imageUrl == null || certificateUrl == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Profile image and certificate are required");
            }

            request.setImgUrl(imageUrl);
            request.setCertification(certificateUrl);

            dietitianService.createDietitian(user, request);

            // 🔥 disable account until admin validation
            user.setEnabled(false);

        } else if ("RESTAURANT".equals(role)) {

            if (user.getRestaurant() != null && user.getRestaurant().isProfileCompleted()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Restaurant profile already completed");
            }

            restaurantService.createRestaurant(user, request);

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


    private String saveFile(MultipartFile file) throws IOException {

    if (file == null || file.isEmpty()) return null;

    String uploadDir = System.getProperty("user.dir") + "/uploads/";

    File folder = new File(uploadDir);
    if (!folder.exists()) folder.mkdirs();

    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

    File destination = new File(uploadDir + fileName);
    file.transferTo(destination);

    return "http://localhost:8080/uploads/" + fileName;
}

}
