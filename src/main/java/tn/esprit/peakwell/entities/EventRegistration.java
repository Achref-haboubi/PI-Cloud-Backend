package tn.esprit.peakwell.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import tn.esprit.peakwell.enums.RegistrationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "event_id"})
        })
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;

    @NotNull(message = "Student ID is required")
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnoreProperties({"registrations", "reviews"})
    private SportEvent event;

    public EventRegistration() {
    }

    public EventRegistration(Long id, LocalDateTime registrationDate, RegistrationStatus status, Long studentId, SportEvent event) {
        this.id = id;
        this.registrationDate = registrationDate;
        this.status = status;
        this.studentId = studentId;
        this.event = event;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
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
