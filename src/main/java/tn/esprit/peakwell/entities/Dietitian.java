package tn.esprit.peakwell.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dietitian {

    @Id
    Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    User user;

    String specialization;
    String certification;
    String linkUrl;

    Integer experienceYears;
    Double consultationPrice;
}