package tn.esprit.peakwell.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    @MapsId
    @JoinColumn(name = "id")
    User user;

    
    String specialization;
    String certification;
    String linkUrl;
    String imgUrl;
    Integer experienceYears;
    Double consultationPrice;

}