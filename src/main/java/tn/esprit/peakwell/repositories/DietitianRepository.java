package tn.esprit.peakwell.repositories;

import com.example.peakwellbackend.entities.Dietitian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietitianRepository extends JpaRepository<Dietitian, Long> {
}
