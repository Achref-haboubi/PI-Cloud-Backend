package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.ReactionDTO;
import tn.esprit.peakwell.services.ReactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reactions")
@CrossOrigin("*")
public class ReactionController {
    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @PostMapping("/article/{articleId}")
    public ResponseEntity<Void> toggleReaction(
            @PathVariable Long articleId,
            @RequestBody Map<String, String> request) {
        String type = request.get("type");
        String userIdentifier = request.get("userIdentifier");
        reactionService.toggleReaction(articleId, type, userIdentifier);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<ReactionDTO>> getReactions(@PathVariable Long articleId) {
        return ResponseEntity.ok(reactionService.getReactionsByArticle(articleId));
    }

    @GetMapping("/article/{articleId}/count")
    public ResponseEntity<Map<String, Long>> getReactionCounts(@PathVariable Long articleId) {
        return ResponseEntity.ok(reactionService.getReactionCounts(articleId));
    }
}
