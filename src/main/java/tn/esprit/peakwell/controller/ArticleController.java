package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.entities.Article;
import tn.esprit.peakwell.dto.ArticleDTO;
import tn.esprit.peakwell.services.ArticleService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/articles")
@CrossOrigin("*")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    //  CREATE
    @PostMapping
    public Article createArticle(
            @RequestParam(value = "title") String title,
            @RequestParam(value = "content") String content,
            @RequestParam(value = "author") String author,
            @RequestParam(value = "embedUrl", required = false) String embedUrl,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = articleService.saveImage(image);
        }
        
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setAuthor(author);
        article.setImageUrl(imageUrl);
        article.setEmbedUrl(embedUrl);

        return articleService.createArticle(article);
    }

    //  GET ALL (Paginated) → Page<ArticleDTO>
    @GetMapping
    public ResponseEntity<Page<ArticleDTO>> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {
        return ResponseEntity.ok(articleService.getAllArticles(page, size));
    }

    //  GET BY ID → DTO
    @GetMapping("/{id}")
    public ArticleDTO getArticle(@PathVariable Long id) {
        return articleService.getArticleById(id);
    }

    //  UPDATE
    @PutMapping("/{id}")
    public Article updateArticle(
            @PathVariable Long id,
            @RequestParam(value = "title") String title,
            @RequestParam(value = "content") String content,
            @RequestParam(value = "author") String author,
            @RequestParam(value = "embedUrl", required = false) String embedUrl,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        
        ArticleDTO existingArticle = articleService.getArticleById(id);
        
        String imageUrl = existingArticle.getImageUrl();
        if (image != null && !image.isEmpty()) {
            if (imageUrl != null) {
                articleService.deleteImage(imageUrl);
            }
            imageUrl = articleService.saveImage(image);
        }
        
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setAuthor(author);
        article.setImageUrl(imageUrl);
            article.setEmbedUrl(embedUrl);

        Article updatedArticle = articleService.updateArticle(id, article);
        return articleService.mapArticleToDTO(updatedArticle);  
    }

    //  DELETE
    @DeleteMapping("/{id}")
    public void deleteArticle(@PathVariable Long id) {
        ArticleDTO article = articleService.getArticleById(id);
        if (article.getImageUrl() != null) {
            articleService.deleteImage(article.getImageUrl());
        }
        articleService.deleteArticle(id);
    }

    //  GET BY AUTHOR → DTO
    @GetMapping("/author/{author}")
    public List<ArticleDTO> getArticlesByAuthor(@PathVariable String author) {
        return articleService.getArticlesByAuthor(author);
    }

    //  SEARCH BY TITLE → DTO
    @GetMapping("/search/{title}")
    public List<ArticleDTO> searchArticlesByTitle(@PathVariable String title) {
        return articleService.searchArticlesByTitle(title);
    }

    //  SERVE IMAGES
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
        Resource resource = articleService.getImageAsResource(filename);
        
        if (resource.exists()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ArticleDTO>> searchArticles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false, defaultValue = "recent") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        return ResponseEntity.ok(
                articleService.searchArticles(search, author, dateFilter, sortBy, page, size));
    }
}