package tn.esprit.peakwell.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level= AccessLevel.PRIVATE)
@Data
public class ProfileRequest {

    private String role;

    // Student
    private Float height;
    private Float weight;
    private String activityLevel;
    private String goal;

    // Shared image
    private String imgUrl;

    // Dietitian
    private String specialization;
    private String certification; // certificate URL
    private String linkUrl;
    private Integer experienceYears;
    private Double consultationPrice;

    // Restaurant
    private String name;
    private String numTeleph;
    private String address;
}