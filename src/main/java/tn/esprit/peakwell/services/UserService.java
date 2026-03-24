package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

}
