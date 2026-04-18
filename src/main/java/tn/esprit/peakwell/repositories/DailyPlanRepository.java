package tn.esprit.peakwell.repositories;

import tn.esprit.peakwell.entities.DailyPlan;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long>{

    Optional<DailyPlan> findByUserIdAndDate(Long userId, LocalDate date);
    
}
