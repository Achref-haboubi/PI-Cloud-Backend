package tn.esprit.peakwell.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private double calories;
    private double protein;
    private double carbs;
    private double fats;
}