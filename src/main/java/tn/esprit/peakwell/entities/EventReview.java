package tn.esprit.peakwell.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_reviews",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "event_id"})
        })
public class EventReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime reviewDate = LocalDateTime.now();

    @NotNull(message = "Student ID is required")
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnoreProperties({"registrations", "reviews"})
    private SportEvent event;

    public EventReview() {
    }

    public EventReview(Long id, Integer rating, String comment, LocalDateTime reviewDate, Long studentId, SportEvent event) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.studentId = studentId;
        this.event = event;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull(message = "Rating is required") @Min(value = 1, message = "Rating must be between 1 and 5") @Max(value = 5, message = "Rating must be between 1 and 5") Integer getRating() {
        return rating;
    }

    public void setRating(@NotNull(message = "Rating is required") @Min(value = 1, message = "Rating must be between 1 and 5") @Max(value = 5, message = "Rating must be between 1 and 5") Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public @NotNull(message = "Student ID is required") Long getStudentId() {
        return studentId;
    }

    public void setStudentId(@NotNull(message = "Student ID is required") Long studentId) {
        this.studentId = studentId;
    }

    public SportEvent getEvent() {
        return event;
    }

    public void setEvent(SportEvent event) {
        this.event = event;
    }
}
