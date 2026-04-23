package tn.esprit.peakwell.repositories;

import tn.esprit.peakwell.entities.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  // Dietitian-scoped queries (for backoffice)
  List<Consultation> findByDietitianIdAndStatusNotOrderByScheduledAtDesc(Long dietitianId, String status);
  List<Consultation> findByDietitianIdAndStatusOrderByScheduledAtAsc(Long dietitianId, String status);

  /**
   * All WAITLISTED consultations for a dietitian, ordered by priority (URGENT first) then by createdAt.
   * This defines the Option-B promotion order.
   */
  @Query("""
      SELECT c FROM Consultation c
      WHERE c.dietitian.id = :dietitianId
        AND c.status = 'WAITLISTED'
      ORDER BY
        CASE c.priority
          WHEN 'URGENT' THEN 1
          WHEN 'HIGH'   THEN 2
          WHEN 'NORMAL' THEN 3
          WHEN 'LOW'    THEN 4
          ELSE 5
        END ASC,
        c.createdAt ASC
      """)
  List<Consultation> findWaitlistedByDietitian(@Param("dietitianId") Long dietitianId);

  /** Distinct students who have at least one non-cancelled consultation with this dietitian */
  @Query("""
      SELECT DISTINCT c.profile.student FROM Consultation c
      WHERE c.dietitian.id = :dietitianId
        AND c.status != 'CANCELLED'
        AND c.profile IS NOT NULL
        AND c.profile.student IS NOT NULL
      """)
  List<tn.esprit.peakwell.entities.Student> findDistinctStudentsByDietitianId(@Param("dietitianId") Long dietitianId);

  /** Check if the dietitian already has a confirmed (UPCOMING) consultation overlapping the given window */
  @Query("""
      SELECT COUNT(c) > 0 FROM Consultation c
      WHERE c.dietitian.id = :dietitianId
        AND c.status = 'UPCOMING'
        AND c.scheduledAt < :windowEnd
        AND timestampadd(minute, c.durationMinutes, c.scheduledAt) > :windowStart
      """)
  boolean existsConflict(@Param("dietitianId") Long dietitianId,
                         @Param("windowStart") LocalDateTime windowStart,
                         @Param("windowEnd") LocalDateTime windowEnd);
}