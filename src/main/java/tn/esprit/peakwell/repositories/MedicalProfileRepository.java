package tn.esprit.peakwell.repositories;

import tn.esprit.peakwell.entities.MedicalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalProfileRepository extends JpaRepository<MedicalProfile, Long> {}
