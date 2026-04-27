package tn.esprit.peakwell.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.peakwell.dto.AdminEventRegistrationDto;
import tn.esprit.peakwell.entities.EventRegistration;
import tn.esprit.peakwell.entities.SportEvent;
import tn.esprit.peakwell.entities.User;
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

    @GetMapping("/event/{eventId}/admin")
    public ResponseEntity<List<AdminEventRegistrationDto>> getAdminRegistrationsByEventId(@PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.getAdminRegistrationsByEventId(eventId));
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<EventRegistration> createRegistration(@PathVariable Long eventId) {
        return new ResponseEntity<>(registrationService.createRegistration(eventId), HttpStatus.CREATED);
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


    @GetMapping(value = "/ticket/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getTicketPage(@PathVariable Long id) {
        EventRegistration reg = registrationService.getRegistrationById(id);

        User user = reg.getStudent().getUser();
        SportEvent event = reg.getEvent();

        String studentName = user.getFirstName() + " " + user.getLastName();

        String html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>PeakWell Ticket</title>
        </head>
        <body style="margin:0;background:#f5f1ed;font-family:Segoe UI,Arial,sans-serif;">
          <div style="max-width:420px;margin:40px auto;background:white;border-radius:24px;
                      box-shadow:0 10px 35px rgba(0,0,0,.12);overflow:hidden;">
            <div style="background:linear-gradient(135deg,#c96a3f,#e88f68);padding:28px;text-align:center;color:white;">
              <h1 style="margin:0;">🎟️ PeakWell Ticket</h1>
              <p style="margin:8px 0 0;">Event Registration Pass</p>
            </div>
            <div style="padding:28px;">
              <p><strong>Student:</strong> %s</p>
              <p><strong>Event:</strong> %s</p>
              <p><strong>Date:</strong> %s</p>
              <p><strong>Location:</strong> %s</p>
              <p><strong>Status:</strong> %s</p>
              <div style="margin-top:24px;padding:14px;border-radius:16px;background:#ecfdf3;color:#15803d;text-align:center;font-weight:700;">
                ✅ Valid PeakWell Ticket
              </div>
            </div>
          </div>
        </body>
        </html>
        """.formatted(
                studentName,
                event.getTitle(),
                event.getEventDate(),
                event.getLocation(),
                reg.getStatus()
        );

        return ResponseEntity.ok(html);
    }
}