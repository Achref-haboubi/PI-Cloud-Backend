package tn.esprit.peakwell.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.peakwell.entities.SportEvent;

@Repository
public interface SportEventRepository extends JpaRepository<SportEvent, Long> {
}
