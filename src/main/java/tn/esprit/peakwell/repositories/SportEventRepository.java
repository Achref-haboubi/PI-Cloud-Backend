package tn.esprit.peakwell.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.peakwell.entities.SportEvent;

@Repository
public interface SportEventRepository extends JpaRepository<SportEvent, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE SportEvent e SET e.status = 'FINISHED' " +
            "WHERE e.eventDate < CURRENT_TIMESTAMP AND e.status <> 'FINISHED'")
    void updateExpiredEvents();
}