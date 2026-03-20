package tn.esprit.peakwell.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Meal name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    private double totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFats;

    private String tags;

    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL)
    @Size(min = 1, message = "Meal must have at least one ingredient")
    private List<Ingredient> ingredients;
}