package tn.esprit.peakwell.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Restaurant {
    
    @Id
    Long id;

    @OneToOne
    @JsonIgnore
    @MapsId
    @JoinColumn(name = "id")
    User user;

    String name;
    String numTeleph;
    String address;


    @Column(nullable = false)
    boolean profileCompleted = false;
}
