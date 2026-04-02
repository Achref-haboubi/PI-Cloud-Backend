package tn.esprit.peakwell.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;


@Getter
@Setter
@FieldDefaults(level= AccessLevel.PRIVATE)
@Data
public class UpdateProfileRequest {

    String firstName;
    String lastName;

    // Student
    Float height;
    Float weight;
    String activityLevel;
    String goal;

    // Shared image
    String imgUrl;

    // Dietitian
    String specialization;
    String certification; // certificate URL
    String linkUrl;
    Integer experienceYears;
    Double consultationPrice;
}
