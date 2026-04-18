package tn.esprit.peakwell.services;

import org.springframework.stereotype.Service;
import tn.esprit.peakwell.entities.EventRegistration;
import tn.esprit.peakwell.entities.EventReview;
import tn.esprit.peakwell.entities.SportEvent;
import tn.esprit.peakwell.enums.EventStatus;
import tn.esprit.peakwell.enums.RegistrationStatus;
import tn.esprit.peakwell.repositories.EventRegistrationRepository;
import tn.esprit.peakwell.repositories.EventReviewRepository;
import tn.esprit.peakwell.repositories.SportEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventReviewService {

    private final EventReviewRepository reviewRepository;
    private final SportEventRepository sportEventRepository;
    private final EventRegistrationRepository registrationRepository;

    public EventReviewService(EventReviewRepository reviewRepository,
                              SportEventRepository sportEventRepository,
                              EventRegistrationRepository registrationRepository) {
        this.reviewRepository = reviewRepository;
        this.sportEventRepository = sportEventRepository;
        this.registrationRepository = registrationRepository;
    }

    private void syncExpiredEventsAndRegistrations() {
        sportEventRepository.updateExpiredEvents();
        registrationRepository.updateConfirmedRegistrationsToAttended();
    }

    public List<EventReview> getAllReviews() {
        syncExpiredEventsAndRegistrations();
        return reviewRepository.findAll();
    }

    public EventReview getReviewById(Long id) {
        syncExpiredEventsAndRegistrations();

        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }

    public List<EventReview> getReviewsByStudentId(Long studentId) {
        syncExpiredEventsAndRegistrations();
        return reviewRepository.findByStudentId(studentId);
    }

    public List<EventReview> getReviewsByEventId(Long eventId) {
        syncExpiredEventsAndRegistrations();
        return reviewRepository.findByEventId(eventId);
    }

    public EventReview createReview(Long eventId, EventReview review) {
        syncExpiredEventsAndRegistrations();

        SportEvent event = sportEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (event.getStatus() != EventStatus.FINISHED) {
            throw new IllegalArgumentException("Review is allowed only after event completion.");
        }

        EventRegistration registration = registrationRepository
                .findByStudentIdAndEventId(review.getStudentId(), eventId)
                .orElseThrow(() -> new IllegalArgumentException("Student did not register for this event."));

        if (registration.getStatus() != RegistrationStatus.ATTENDED) {
            throw new IllegalArgumentException("Only attended students can review this event.");
        }

        reviewRepository.findByStudentIdAndEventId(review.getStudentId(), eventId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("This student already reviewed this event.");
                });

        review.setEvent(event);
        review.setReviewDate(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public EventReview updateReview(Long id, EventReview updatedReview) {
        syncExpiredEventsAndRegistrations();

        EventReview existingReview = getReviewById(id);

        existingReview.setRating(updatedReview.getRating());
        existingReview.setComment(updatedReview.getComment());

        return reviewRepository.save(existingReview);
    }

    public void deleteReview(Long id) {
        EventReview review = getReviewById(id);
        reviewRepository.delete(review);
    }
}