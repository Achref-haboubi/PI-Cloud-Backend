package tn.esprit.peakwell.services;

import org.springframework.stereotype.Service;
import tn.esprit.peakwell.entities.SportEvent;
import tn.esprit.peakwell.enums.EventStatus;
import tn.esprit.peakwell.repositories.SportEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SportEventService {

    private final SportEventRepository sportEventRepository;

    public SportEventService(SportEventRepository sportEventRepository) {
        this.sportEventRepository = sportEventRepository;
    }

    public List<SportEvent> getAllEvents() {
        // Met à jour en base les événements expirés
        sportEventRepository.updateExpiredEvents();

        List<SportEvent> events = sportEventRepository.findAll();

        for (SportEvent event : events) {
            applyRuntimeStatus(event);
        }

        return events;
    }

    public SportEvent getEventById(Long id) {
        // Met à jour en base les événements expirés
        sportEventRepository.updateExpiredEvents();

        SportEvent event = sportEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        applyRuntimeStatus(event);
        return event;
    }

    public SportEvent createEvent(SportEvent event) {
        if (event.getCurrentParticipants() == null) {
            event.setCurrentParticipants(0);
        }

        event.updateStatusBasedOnCapacity();
        return sportEventRepository.save(event);
    }

    public SportEvent updateEvent(Long id, SportEvent updatedEvent) {
        sportEventRepository.updateExpiredEvents();

        SportEvent existingEvent = sportEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        if (isExpired(existingEvent) || existingEvent.getStatus() == EventStatus.FINISHED) {
            throw new IllegalArgumentException("Finished events cannot be modified.");
        }

        existingEvent.setTitle(updatedEvent.getTitle());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setEventDate(updatedEvent.getEventDate());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setLatitude(updatedEvent.getLatitude());
        existingEvent.setLongitude(updatedEvent.getLongitude());
        existingEvent.setCategory(updatedEvent.getCategory());
        existingEvent.setEventDetail(updatedEvent.getEventDetail());
        existingEvent.setMaxParticipants(updatedEvent.getMaxParticipants());
        existingEvent.setCurrentParticipants(updatedEvent.getCurrentParticipants());
        existingEvent.setImageUrl(updatedEvent.getImageUrl());

        // appliquer le status demandé par le front
        if (updatedEvent.getStatus() != null) {
            existingEvent.setStatus(updatedEvent.getStatus());
        }

        // si ce n'est pas annulé ni terminé, recalcul automatique OPEN/FULL
        if (existingEvent.getStatus() != EventStatus.CANCELLED &&
                existingEvent.getStatus() != EventStatus.FINISHED) {
            existingEvent.updateStatusBasedOnCapacity();
        }

        return sportEventRepository.save(existingEvent);
    }
    public void deleteEvent(Long id) {
        SportEvent event = sportEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        sportEventRepository.delete(event);
    }

    private boolean isExpired(SportEvent event) {
        return event.getEventDate() != null && event.getEventDate().isBefore(LocalDateTime.now());
    }

    private void applyRuntimeStatus(SportEvent event) {
        if (isExpired(event)) {
            event.setStatus(EventStatus.FINISHED);
        } else {
            event.updateStatusBasedOnCapacity();
        }
    }
}