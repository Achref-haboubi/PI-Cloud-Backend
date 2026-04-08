package tn.esprit.peakwell.dto;

import lombok.*;
import tn.esprit.peakwell.entities.Category_Product;

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

    private Category_Product category_Product;
    private double stock;
    private String unit;
    private String image;

    private double minStock;
    private String stockStatus;
}