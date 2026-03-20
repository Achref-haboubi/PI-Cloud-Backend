package tn.esprit.peakwell.controller;



import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.peakwell.entities.SportEvent;
import tn.esprit.peakwell.services.SportEventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin("*")
public class SportEventController {

    private final SportEventService sportEventService;

    public SportEventController(SportEventService sportEventService) {
        this.sportEventService = sportEventService;
    }

    @GetMapping
    public ResponseEntity<List<SportEvent>> getAllEvents() {
        return ResponseEntity.ok(sportEventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SportEvent> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(sportEventService.getEventById(id));
    }

    @PostMapping
    public ResponseEntity<SportEvent> createEvent(@Valid @RequestBody SportEvent event) {
        return new ResponseEntity<>(sportEventService.createEvent(event), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SportEvent> updateEvent(@PathVariable Long id,
                                                  @Valid @RequestBody SportEvent event) {
        return ResponseEntity.ok(sportEventService.updateEvent(id, event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
        sportEventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully.");
    }
}
