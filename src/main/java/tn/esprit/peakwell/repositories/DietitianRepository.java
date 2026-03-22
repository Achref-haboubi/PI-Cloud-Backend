package tn.esprit.peakwell.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.peakwell.entities.Dietitian;

public interface DietitianRepository extends JpaRepository<Dietitian, Long> {
}
