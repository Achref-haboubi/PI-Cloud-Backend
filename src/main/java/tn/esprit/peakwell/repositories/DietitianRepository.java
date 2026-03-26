package tn.esprit.peakwell.repositories;

import tn.esprit.peakwell.entities.Dietitian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietitianRepository extends JpaRepository<Dietitian, Long> {
}
