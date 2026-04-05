package tn.esprit.peakwell.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tn.esprit.peakwell.enums.EventCategory;
import tn.esprit.peakwell.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sport_events")
public class SportEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    @Column(nullable = false)
    private LocalDateTime eventDate;

    @NotBlank(message = "Location is required")
    @Column(nullable = false)
    private String location;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @Column(length = 500)
    private String eventDetail;

    @NotNull(message = "Max participants is required")
    @Min(value = 1, message = "Max participants must be at least 1")
    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(nullable = false)
    private Integer currentParticipants = 0;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.OPEN;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("event")
    private List<EventRegistration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("event")
    private List<EventReview> reviews = new ArrayList<>();

    public SportEvent() {
    }

    public void updateStatusBasedOnCapacity() {
        if (this.eventDate != null && this.eventDate.isBefore(LocalDateTime.now())) {
            this.status = EventStatus.FINISHED;
            return;
        }

        if (this.currentParticipants != null && this.maxParticipants != null) {
            if (this.currentParticipants >= this.maxParticipants) {
                this.status = EventStatus.FULL;
            } else if (this.status != EventStatus.CANCELLED && this.status != EventStatus.FINISHED) {
                this.status = EventStatus.OPEN;
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Title is required") String getTitle() {
        return title;
    }

    public void setTitle(@NotBlank(message = "Title is required") String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public @NotNull(message = "Event date is required") @Future(message = "Event date must be in the future") LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(@NotNull(message = "Event date is required") @Future(message = "Event date must be in the future") LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public @NotBlank(message = "Location is required") String getLocation() {
        return location;
    }

    public void setLocation(@NotBlank(message = "Location is required") String location) {
        this.location = location;
    }

    public @NotNull(message = "Category is required") EventCategory getCategory() {
        return category;
    }

    public void setCategory(@NotNull(message = "Category is required") EventCategory category) {
        this.category = category;
    }

    public String getEventDetail() {
        return eventDetail;
    }

    public void setEventDetail(String eventDetail) {
        this.eventDetail = eventDetail;
    }

    public @NotNull(message = "Max participants is required") @Min(value = 1, message = "Max participants must be at least 1") Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(@NotNull(message = "Max participants is required") @Min(value = 1, message = "Max participants must be at least 1") Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public List<EventRegistration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<EventRegistration> registrations) {
        this.registrations = registrations;
    }

    public List<EventReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<EventReview> reviews) {
        this.reviews = reviews;
    }
}