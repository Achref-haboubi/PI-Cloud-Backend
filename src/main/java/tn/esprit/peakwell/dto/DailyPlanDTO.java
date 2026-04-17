package tn.esprit.peakwell.dto;

import lombok.*;
import tn.esprit.peakwell.entities.Meal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyPlanDTO {
    private Meal breakfast;
    private Meal lunch;
    private Meal dinner;

    private double totalCalories;
    private String status;
}
