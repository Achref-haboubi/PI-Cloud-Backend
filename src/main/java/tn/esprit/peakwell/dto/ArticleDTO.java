package tn.esprit.peakwell.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {

    private Long id;
    private String title;
    private String content;
    private String author;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}