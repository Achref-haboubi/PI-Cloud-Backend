package tn.esprit.peakwell.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level= AccessLevel.PRIVATE)
public class ProfileRequest {

    private String role;

    // STUDENT
     Float height;
     Float weight;
     String activityLevel;
     String goal;

    // DIETITIAN
     String specialization;
     String certification;
     String linkUrl;
     String imgUrl;
     Integer experienceYears;
     Double consultationPrice;
}
