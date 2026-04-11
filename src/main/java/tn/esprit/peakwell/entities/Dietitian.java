package tn.esprit.peakwell.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "patients")
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
  Integer experienceYears;
  Double consultationPrice;

  @JsonIgnore
  @OneToMany(mappedBy = "assignedDietitian")
  List<MedicalProfile> patients = new ArrayList<>();
}
