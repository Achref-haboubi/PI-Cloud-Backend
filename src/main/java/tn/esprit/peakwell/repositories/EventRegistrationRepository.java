package tn.esprit.peakwell.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.peakwell.entities.EventRegistration;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    List<EventRegistration> findByStudentId(Long studentId);

    List<EventRegistration> findByEventId(Long eventId);

    Optional<EventRegistration> findByStudentIdAndEventId(Long studentId, Long eventId);
}
