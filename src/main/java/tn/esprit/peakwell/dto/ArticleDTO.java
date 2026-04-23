package tn.esprit.peakwell.dto;

import lombok.*;
import tn.esprit.peakwell.entities.Article;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO extends Article {

    private Long id;
    private String title;
    private String content;
    private String author;
    private String imageUrl;
    private String embedUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}