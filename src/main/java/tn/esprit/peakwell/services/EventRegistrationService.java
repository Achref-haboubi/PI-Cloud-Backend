package tn.esprit.peakwell.services;



import org.springframework.stereotype.Service;
import tn.esprit.peakwell.entities.EventRegistration;
import tn.esprit.peakwell.entities.SportEvent;
import tn.esprit.peakwell.enums.EventStatus;
import tn.esprit.peakwell.enums.RegistrationStatus;
import tn.esprit.peakwell.repositories.EventRegistrationRepository;
import tn.esprit.peakwell.repositories.SportEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventRegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final SportEventRepository sportEventRepository;

    public EventRegistrationService(EventRegistrationRepository registrationRepository,
                                    SportEventRepository sportEventRepository) {
        this.registrationRepository = registrationRepository;
        this.sportEventRepository = sportEventRepository;
    }

    public List<EventRegistration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    public EventRegistration getRegistrationById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regestration not found with id: " + id));
    }

    public List<EventRegistration> getRegistrationsByStudentId(Long studentId) {
        return registrationRepository.findByStudentId(studentId);
    }

    public List<EventRegistration> getRegistrationsByEventId(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    public EventRegistration createRegistration(Long eventId, EventRegistration registration) {
        SportEvent event = sportEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        registrationRepository.findByStudentIdAndEventId(registration.getStudentId(), eventId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("This student is already registered for this event.");
                });

        registration.setEvent(event);
        registration.setRegistrationDate(LocalDateTime.now());

        if (event.getCurrentParticipants() < event.getMaxParticipants()) {
            registration.setStatus(RegistrationStatus.CONFIRMED);
            event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        } else {
            registration.setStatus(RegistrationStatus.WAITING);
            event.setStatus(EventStatus.FULL);
        }

        event.updateStatusBasedOnCapacity();
        sportEventRepository.save(event);

        return registrationRepository.save(registration);
    }

    public EventRegistration updateRegistrationStatus(Long id, RegistrationStatus status) {
        EventRegistration registration = getRegistrationById(id);
        registration.setStatus(status);
        return registrationRepository.save(registration);
    }

    public void deleteRegistration(Long id) {
        EventRegistration registration = getRegistrationById(id);
        SportEvent event = registration.getEvent();

        if (registration.getStatus() == RegistrationStatus.CONFIRMED && event.getCurrentParticipants() > 0) {
            event.setCurrentParticipants(event.getCurrentParticipants() - 1);
            event.updateStatusBasedOnCapacity();
            sportEventRepository.save(event);
        }

        registrationRepository.delete(registration);
    }
}
