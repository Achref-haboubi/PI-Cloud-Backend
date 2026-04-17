package tn.esprit.peakwell.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class DailyPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    private Long breakfastId;
    private Long lunchId;
    private Long dinnerId;

    private double totalCalories;

    private String status;


    
}
