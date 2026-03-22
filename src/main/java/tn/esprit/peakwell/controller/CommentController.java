package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.entities.Comment;
import tn.esprit.peakwell.dto.CommentDTO;
import tn.esprit.peakwell.services.CommentService;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin("*")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // ✅ ADD COMMENT
    @PostMapping("/article/{articleId}")
    public Comment addComment(@PathVariable Long articleId, @Valid @RequestBody CommentDTO commentDTO) {
        return commentService.addComment(articleId, commentDTO);
    }

    // ✅ GET ALL COMMENTS OF AN ARTICLE → DTO
    @GetMapping("/article/{articleId}")
    public List<CommentDTO> getCommentsByArticle(@PathVariable Long articleId) {
        return commentService.getCommentsByArticle(articleId);
    }

    // ✅ DELETE COMMENT
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
    }
}
