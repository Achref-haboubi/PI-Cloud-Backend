package tn.esprit.peakwell.services;


import org.springframework.stereotype.Service;
import tn.esprit.peakwell.entities.EventReview;
import tn.esprit.peakwell.entities.SportEvent;
import tn.esprit.peakwell.repositories.EventReviewRepository;
import tn.esprit.peakwell.repositories.SportEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventReviewService {

    private final EventReviewRepository reviewRepository;
    private final SportEventRepository sportEventRepository;

    public EventReviewService(EventReviewRepository reviewRepository,
                              SportEventRepository sportEventRepository) {
        this.reviewRepository = reviewRepository;
        this.sportEventRepository = sportEventRepository;
    }

    public List<EventReview> getAllReviews() {
        return reviewRepository.findAll();
    }

    public EventReview getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }

    public List<EventReview> getReviewsByStudentId(Long studentId) {
        return reviewRepository.findByStudentId(studentId);
    }

    public List<EventReview> getReviewsByEventId(Long eventId) {
        return reviewRepository.findByEventId(eventId);
    }

    public EventReview createReview(Long eventId, EventReview review) {
        SportEvent event = sportEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        reviewRepository.findByStudentIdAndEventId(review.getStudentId(), eventId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("This student already reviewed this event.");
                });

        review.setEvent(event);
        review.setReviewDate(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public EventReview updateReview(Long id, EventReview updatedReview) {
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
