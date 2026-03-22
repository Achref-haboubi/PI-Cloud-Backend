package tn.esprit.peakwell.services;

import tn.esprit.peakwell.entities.Comment;
import tn.esprit.peakwell.entities.Article;
import tn.esprit.peakwell.repositories.CommentRepository;
import tn.esprit.peakwell.repositories.ArticleRepository;
import tn.esprit.peakwell.dto.CommentDTO;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    public CommentService(CommentRepository commentRepository, ArticleRepository articleRepository) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
    }

    // ✅ ADD COMMENT
    public Comment addComment(Long articleId, CommentDTO commentDTO) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + articleId));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setAuthor(commentDTO.getAuthor());
        comment.setArticle(article);

        return commentRepository.save(comment);
    }

    // ✅ GET COMMENTS BY ARTICLE → DTO
    public List<CommentDTO> getCommentsByArticle(Long articleId) {
        return commentRepository.findByArticleId(articleId)
                .stream()
                .map(this::mapCommentToDTO)
                .toList();
    }

    // ✅ DELETE COMMENT
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        commentRepository.delete(comment);
    }

    // 🔁 MAPPER Comment → DTO
    private CommentDTO mapCommentToDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor(),
                comment.getCreatedAt(),
                comment.getArticle().getId()
        );
    }
}
