package tn.esprit.peakwell.repositories;

import tn.esprit.peakwell.entities.MedicalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalProfileRepository extends JpaRepository<MedicalProfile, Long> {
  Optional<MedicalProfile> findByStudentId(Long studentId);
  List<MedicalProfile> findByAssignedDietitianId(Long dietitianId);
  Optional<MedicalProfile> findFirstByOrderByIdAsc();
}
