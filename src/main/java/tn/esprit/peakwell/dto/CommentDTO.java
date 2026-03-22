package tn.esprit.peakwell.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private Long id;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private Long articleId;
}
