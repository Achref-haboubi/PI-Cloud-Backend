package tn.esprit.peakwell.controller;



import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.peakwell.entities.SportEvent;
import tn.esprit.peakwell.services.SportEventService;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Fichier vide");
            }

            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.write(filePath, file.getBytes());

            String imageUrl = "http://localhost:8090/peakwell/uploads/" + fileName;
            return ResponseEntity.ok(imageUrl);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload de l'image");
        }
    }
}
