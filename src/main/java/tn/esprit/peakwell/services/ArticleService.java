package tn.esprit.peakwell.services;

import tn.esprit.peakwell.entities.Article;
import tn.esprit.peakwell.repositories.ArticleRepository;
import tn.esprit.peakwell.dto.ArticleDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final String UPLOAD_DIR = "uploads/articles";

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    // CREATE
    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    //  GET ALL (Paginated) → Page<DTO>
    public Page<ArticleDTO> getAllArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return articleRepository.findAll(pageable)
                .map(this::mapArticleToDTO);
    }

    //  GET ALL (Non-paginated) → List<DTO> [Backward compatibility]
    public List<ArticleDTO> getAllArticlesAsList() {
        return articleRepository.findAll()
                .stream()
                .map(this::mapArticleToDTO)
                .toList();
    }

    //  GET BY ID → DTO
    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + id));
        return mapArticleToDTO(article);
    }

    //  UPDATE
    public Article updateArticle(Long id, Article articleDetails) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + id));

        article.setTitle(articleDetails.getTitle());
        article.setContent(articleDetails.getContent());
        article.setAuthor(articleDetails.getAuthor());
        
        if (articleDetails.getImageUrl() != null) {
            article.setImageUrl(articleDetails.getImageUrl());
        }

        if (articleDetails.getEmbedUrl() != null) {
            article.setEmbedUrl(articleDetails.getEmbedUrl());
        }

        return articleRepository.save(article);
    }

    //  DELETE
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + id));
        articleRepository.delete(article);
    }

    //  GET BY AUTHOR → DTO
    public List<ArticleDTO> getArticlesByAuthor(String author) {
        return articleRepository.findByAuthor(author)
                .stream()
                .map(this::mapArticleToDTO)
                .toList();
    }

    //  GET BY TITLE CONTAINING → DTO
    public List<ArticleDTO> searchArticlesByTitle(String title) {
        return articleRepository.findByTitleContaining(title)
                .stream()
                .map(this::mapArticleToDTO)
                .toList();
    }

    //  MAPPER Article → DTO
    public ArticleDTO mapArticleToDTO(Article article) {
        return new ArticleDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getAuthor(),
                article.getImageUrl(),
                article.getEmbedUrl(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }

    // SAVE IMAGE
    public String saveImage(MultipartFile file) throws IOException {
        Path uploadsPath = Paths.get(UPLOAD_DIR);
        
        // Create directory if doesn't exist
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }
        
        // Generate unique filename using UUID
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : ".jpg";
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = uploadsPath.resolve(uniqueFileName);
        Files.write(filePath, file.getBytes());
        
        return uniqueFileName;
    }

    //  DELETE IMAGE
    public void deleteImage(String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error deleting image: " + e.getMessage());
        }
    }

    //  GET IMAGE AS RESOURCE
    public Resource getImageAsResource(String filename) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
        return new UrlResource(filePath.toUri());
    }
}