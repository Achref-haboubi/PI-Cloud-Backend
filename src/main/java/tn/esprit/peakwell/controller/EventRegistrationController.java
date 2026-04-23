package tn.esprit.peakwell.controller;



import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.peakwell.entities.EventRegistration;
import tn.esprit.peakwell.enums.RegistrationStatus;
import tn.esprit.peakwell.services.EventRegistrationService;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin("*")
public class EventRegistrationController {

    private final EventRegistrationService registrationService;

    public EventRegistrationController(EventRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public ResponseEntity<List<EventRegistration>> getAllRegistrations() {
        return ResponseEntity.ok(registrationService.getAllRegistrations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventRegistration> getRegistrationById(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.getRegistrationById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EventRegistration>> getByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(registrationService.getRegistrationsByStudentId(studentId));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventRegistration>> getByEventId(@PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.getRegistrationsByEventId(eventId));
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<EventRegistration> createRegistration(@PathVariable Long eventId,
                                                                @Valid @RequestBody EventRegistration registration) {
        return new ResponseEntity<>(registrationService.createRegistration(eventId, registration), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<EventRegistration> updateRegistrationStatus(@PathVariable Long id,
                                                                      @RequestParam RegistrationStatus status) {
        return ResponseEntity.ok(registrationService.updateRegistrationStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRegistration(@PathVariable Long id) {
        registrationService.deleteRegistration(id);
        return ResponseEntity.ok("Registration deleted successfully.");
    }
}
