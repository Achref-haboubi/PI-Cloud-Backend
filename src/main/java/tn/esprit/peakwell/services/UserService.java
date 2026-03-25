package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.peakwell.dto.ProfileRequest;
import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final AuthService authService;
    private final UserRepository userRepository;
    private final StudentService studentService;
    private final DietitianService dietitianService;

    @Override
    public void completeProfile(ProfileRequest request) {

        String keycloakId = authService.getCurrentUserId();

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Prevent duplicate profile
        if (user.isProfileCompleted()) {
            throw new RuntimeException("Profile already completed");
        }

        //  Role-based creation
        switch (request.getRole()) {

            case "STUDENT" -> studentService.createStudent(user, request);

            case "DIETITIAN" -> dietitianService.createDietitian(user, request);

            default -> throw new RuntimeException("Invalid role");
        }

        user.setProfileCompleted(true);

        userRepository.save(user);
    }

    @Override
    public User updateProfile(ProfileRequest request) {

        try {

            String keycloakId = authService.getCurrentUserId(); //  may throw 401

            User user = userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                    );

            //  profile not completed
            if (!user.isProfileCompleted()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Complete profile first"
                );
            }

            //  block inactive dietitian
            if (user.getDietitian() != null && Boolean.FALSE.equals(user.getDietitian().isActive())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Account pending admin approval"
                );
            }

            //  role-based update
            if (user.getStudent() != null) {
                studentService.updateStudent(user, request);
            } else if (user.getDietitian() != null) {
                dietitianService.updateDietitian(user, request);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No profile found"
                );
            }

            return userRepository.save(user);

        } catch (ResponseStatusException ex) {
            throw ex; // keep business errors

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal server error"
            );
        }
    }
}
