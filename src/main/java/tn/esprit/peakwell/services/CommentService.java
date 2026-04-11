package tn.esprit.peakwell.services;

import tn.esprit.peakwell.entities.Comment;
import tn.esprit.peakwell.entities.Article;
import tn.esprit.peakwell.entities.CommentVote;
import tn.esprit.peakwell.repositories.CommentRepository;
import tn.esprit.peakwell.repositories.ArticleRepository;
import tn.esprit.peakwell.repositories.CommentVoteRepository;
import tn.esprit.peakwell.dto.CommentDTO;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final CommentVoteRepository commentVoteRepository;

    public CommentService(CommentRepository commentRepository,
                          ArticleRepository articleRepository,
                          CommentVoteRepository commentVoteRepository) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.commentVoteRepository = commentVoteRepository;
    }

    // ✅ ADD COMMENT
    public Comment addComment(Long articleId, CommentDTO commentDTO) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + articleId));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setAuthor(commentDTO.getAuthor());
        comment.setArticle(article);
        comment.setUpvotes(0);
        comment.setDownvotes(0);

        return commentRepository.save(comment);
    }

    // ✅ GET COMMENTS (ONLY TOP LEVEL + REPLIES)
    public List<CommentDTO> getCommentsByArticle(Long articleId) {
        return commentRepository.findByArticleIdAndParentCommentIsNull(articleId)
                .stream()
                .map(this::mapCommentToDTO)
                .toList();
    }

    // ✅ ADD REPLY
    public CommentDTO addReply(Long articleId, Long parentCommentId, CommentDTO dto) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + articleId));

        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + parentCommentId));

        Comment reply = new Comment();
        reply.setContent(dto.getContent());
        reply.setAuthor(dto.getAuthor());
        reply.setArticle(article);
        reply.setParentComment(parent);
        reply.setUpvotes(0);
        reply.setDownvotes(0);

        return mapCommentToDTO(commentRepository.save(reply));
    }

    // ✅ VOTE COMMENT (UPVOTE / DOWNVOTE)
    public Map<String, Object> voteComment(Long commentId, String voteType, String userIdentifier) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        Optional<CommentVote> existingVote =
                commentVoteRepository.findByCommentIdAndUserIdentifier(commentId, userIdentifier);

        String userVote = null;

        if (existingVote.isPresent()) {

            CommentVote vote = existingVote.get();

            if (vote.getVoteType().equals(voteType)) {
                // 🔁 SAME CLICK → REMOVE VOTE
                if (voteType.equals("UPVOTE")) {
                    comment.setUpvotes(Math.max(0, comment.getUpvotes() - 1));
                } else {
                    comment.setDownvotes(Math.max(0, comment.getDownvotes() - 1));
                }
                commentVoteRepository.delete(vote);

            } else {
                // 🔄 SWITCH VOTE
                if (vote.getVoteType().equals("UPVOTE")) {
                    comment.setUpvotes(Math.max(0, comment.getUpvotes() - 1));
                } else {
                    comment.setDownvotes(Math.max(0, comment.getDownvotes() - 1));
                }

                if (voteType.equals("UPVOTE")) {
                    comment.setUpvotes(comment.getUpvotes() + 1);
                } else {
                    comment.setDownvotes(comment.getDownvotes() + 1);
                }

                vote.setVoteType(voteType);
                commentVoteRepository.save(vote);

                userVote = voteType;
            }

        } else {
            // 🆕 NEW VOTE
            if (voteType.equals("UPVOTE")) {
                comment.setUpvotes(comment.getUpvotes() + 1);
            } else {
                comment.setDownvotes(comment.getDownvotes() + 1);
            }

            CommentVote newVote = new CommentVote();
            newVote.setComment(comment);
            newVote.setVoteType(voteType);
            newVote.setUserIdentifier(userIdentifier);

            commentVoteRepository.save(newVote);

            userVote = voteType;
        }

        commentRepository.save(comment);

        Map<String, Object> res = new HashMap<>();
        res.put("upvotes", comment.getUpvotes());
        res.put("downvotes", comment.getDownvotes());
        res.put("userVote", userVote);

        return res;
    }

    // ✅ DELETE COMMENT
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        commentRepository.delete(comment);
    }

    // 🔁 MAPPER Comment → DTO
    private CommentDTO mapCommentToDTO(Comment comment) {

        CommentDTO dto = new CommentDTO();

        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthor(comment.getAuthor());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setArticleId(comment.getArticle().getId());

        dto.setUpvotes(comment.getUpvotes());
        dto.setDownvotes(comment.getDownvotes());

        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(
                    comment.getReplies()
                            .stream()
                            .map(this::mapCommentToDTO)
                            .toList()
            );
        }

        return dto;
    }
}