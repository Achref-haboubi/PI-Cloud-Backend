package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.SavedArticleDTO;
import tn.esprit.peakwell.services.SavedArticleService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saved-articles")
@CrossOrigin("*")
public class SavedArticleController {
    private final SavedArticleService savedArticleService;

    public SavedArticleController(SavedArticleService savedArticleService) {
        this.savedArticleService = savedArticleService;
    }

    @PostMapping("/toggle")
    public ResponseEntity<Void> toggleSave(@RequestBody Map<String, Object> request) {
        Long articleId = ((Number) request.get("articleId")).longValue();
        String userIdentifier = (String) request.get("userIdentifier");

        savedArticleService.toggleSave(articleId, userIdentifier);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userIdentifier}")
    public ResponseEntity<List<SavedArticleDTO>> getSavedArticles(@PathVariable String userIdentifier) {
        return ResponseEntity.ok(savedArticleService.getSavedArticles(userIdentifier));
    }

    @GetMapping("/check/{articleId}/{userIdentifier}")
    public ResponseEntity<Boolean> checkIfSaved(@PathVariable Long articleId, @PathVariable String userIdentifier) {
        return ResponseEntity.ok(savedArticleService.isSaved(articleId, userIdentifier));
    }
}
