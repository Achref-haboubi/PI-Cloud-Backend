package tn.esprit.peakwell.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile {
    Long id;
    String email;
    String firstName;
    String lastName;
    String role;
    boolean profileCompleted;

    StudentProfile studentProfile;
    DietitianProfile dietitianProfile;
}
