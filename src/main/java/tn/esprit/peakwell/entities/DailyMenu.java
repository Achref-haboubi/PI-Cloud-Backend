package tn.esprit.peakwell.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyMenu {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @OneToOne
    private Meal breakfast;

    @OneToOne
    private Meal lunch;

    @OneToOne
    private Meal dinner;
    
}
