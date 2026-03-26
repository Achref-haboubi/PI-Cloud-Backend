package tn.esprit.peakwell.repositories;

import tn.esprit.peakwell.entities.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
  List<Consultation> findAllByOrderByScheduledAtDesc();
  List<Consultation> findByStatusOrderByScheduledAtAsc(String status);
  List<Consultation> findByProfileIdOrderByScheduledAtDesc(Long profileId);
  List<Consultation> findByScheduledAtAfterOrderByScheduledAtAsc(LocalDateTime date);
  List<Consultation> findByScheduledAtBeforeAndStatusOrderByScheduledAtDesc(LocalDateTime date, String status);
  List<Consultation> findByScheduledAtBeforeAndStatusNotOrderByScheduledAtDesc(LocalDateTime date, String status);
  List<Consultation> findAllByProfileIdOrderByScheduledAtDesc(Long profileId);
}
