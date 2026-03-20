package tn.esprit.peakwell.services;

import org.springframework.stereotype.Service;
import tn.esprit.peakwell.entities.SportEvent;
import tn.esprit.peakwell.repositories.SportEventRepository;

import java.util.List;

@Service
public class SportEventService {

    private final SportEventRepository sportEventRepository;

    public SportEventService(SportEventRepository sportEventRepository) {
        this.sportEventRepository = sportEventRepository;
    }

    public List<SportEvent> getAllEvents() {
        return sportEventRepository.findAll();
    }

    public SportEvent getEventById(Long id) {
        return sportEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    public SportEvent createEvent(SportEvent event) {
        if (event.getCurrentParticipants() == null) {
            event.setCurrentParticipants(0);
        }
        event.updateStatusBasedOnCapacity();
        return sportEventRepository.save(event);
    }

    public SportEvent updateEvent(Long id, SportEvent updatedEvent) {
        SportEvent existingEvent = getEventById(id);

        existingEvent.setTitle(updatedEvent.getTitle());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setEventDate(updatedEvent.getEventDate());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setSportType(updatedEvent.getSportType());
        existingEvent.setMaxParticipants(updatedEvent.getMaxParticipants());
        existingEvent.setCurrentParticipants(updatedEvent.getCurrentParticipants());
        existingEvent.setImageUrl(updatedEvent.getImageUrl());
        existingEvent.setStatus(updatedEvent.getStatus());

        existingEvent.updateStatusBasedOnCapacity();

        return sportEventRepository.save(existingEvent);
    }

    public void deleteEvent(Long id) {
        SportEvent event = getEventById(id);
        sportEventRepository.delete(event);
    }
}
