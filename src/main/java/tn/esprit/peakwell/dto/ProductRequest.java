package tn.esprit.peakwell.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @Positive
    private double calories;

    private double protein;
    private double carbs;
    private double fats;

    @NotBlank
    private String category;

    private String allergens;

    private double stock;

    @NotBlank
    private String unit;

    private String image;
}
