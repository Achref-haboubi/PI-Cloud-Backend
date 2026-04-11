package tn.esprit.peakwell.services;

import tn.esprit.peakwell.entities.Article;
import tn.esprit.peakwell.repositories.ArticleRepository;
import tn.esprit.peakwell.dto.ArticleDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final String UPLOAD_DIR = "uploads/articles";

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    // ✅ CREATE
    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    // ✅ GET ALL (Paginated)
    public Page<ArticleDTO> getAllArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return articleRepository.findAll(pageable)
                .map(this::mapArticleToDTO);
    }

    // ✅ GET ALL (Non-paginated)
    public List<ArticleDTO> getAllArticlesAsList() {
        return articleRepository.findAll()
                .stream()
                .map(this::mapArticleToDTO)
                .toList();
    }

    // ✅ GET BY ID
    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found: " + id));
        return mapArticleToDTO(article);
    }

    // ✅ UPDATE
    public Article updateArticle(Long id, Article articleDetails) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found: " + id));

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

    // ✅ DELETE
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found: " + id));
        articleRepository.delete(article);
    }

    // ✅ GET BY AUTHOR
    public List<ArticleDTO> getArticlesByAuthor(String author) {
        return articleRepository.findByAuthor(author)
                .stream()
                .map(this::mapArticleToDTO)
                .toList();
    }

    // ✅ SEARCH BY TITLE
    public List<ArticleDTO> searchArticlesByTitle(String title) {
        return articleRepository.findByTitleContaining(title)
                .stream()
                .map(this::mapArticleToDTO)
                .toList();
    }

    // ✅ SEARCH ARTICLES WITH FILTERS
    public Page<ArticleDTO> searchArticles(
            String search,
            String author,
            String dateFilter,
            String sortBy,
            int page,
            int size) {

        // Calculate date range
        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now();

        if (dateFilter != null && !dateFilter.isEmpty()) {
            switch (dateFilter) {
                case "week":
                    startDate = LocalDateTime.now().minusWeeks(1);
                    break;
                case "month":
                    startDate = LocalDateTime.now().minusMonths(1);
                    break;
                case "year":
                    startDate = LocalDateTime.now().minusYears(1);
                    break;
                default:
                    startDate = null;
            }
        }

        // Normalize empty strings to null
        String searchParam = (search != null && !search.trim().isEmpty())
                ? search.trim() : null;
        String authorParam = (author != null && !author.trim().isEmpty())
                ? author.trim() : null;

        // For likes/comments sorting: fetch without DB sort, sort in memory
        if ("likes".equals(sortBy) || "comments".equals(sortBy)) {

            // Fetch large page to sort in memory
            Pageable fetchAll = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Article> allResults = articleRepository.searchArticles(
                    searchParam, authorParam, startDate, endDate, fetchAll);

            // Sort in memory
            Comparator<Article> comparator;
            if ("likes".equals(sortBy)) {
                comparator = Comparator.comparingInt(
                        a -> -(a.getReactions() != null ? a.getReactions().size() : 0)
                );
            } else {
                comparator = Comparator.comparingInt(
                        a -> -(a.getComments() != null ? a.getComments().size() : 0)
                );
            }

            List<ArticleDTO> sorted = allResults.getContent()
                    .stream()
                    .sorted(comparator)
                    .map(this::mapArticleToDTO)
                    .collect(Collectors.toList());

            // Manual pagination on sorted list
            int start = page * size;
            int end = Math.min(start + size, sorted.size());

            if (start >= sorted.size()) {
                return new PageImpl<>(
                        List.of(),
                        PageRequest.of(page, size),
                        sorted.size()
                );
            }

            return new PageImpl<>(
                    sorted.subList(start, end),
                    PageRequest.of(page, size),
                    sorted.size()
            );

        } else {
            // Default: sort by date DESC
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by(Sort.Direction.DESC, "createdAt"));

            return articleRepository.searchArticles(
                            searchParam, authorParam, startDate, endDate, pageable)
                    .map(this::mapArticleToDTO);
        }
    }

    // ✅ MAPPER Article → DTO
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

    // ✅ SAVE IMAGE
    public String saveImage(MultipartFile file) throws IOException {
        Path uploadsPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName != null
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : ".jpg";
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadsPath.resolve(uniqueFileName);
        Files.write(filePath, file.getBytes());
        return uniqueFileName;
    }

    // ✅ DELETE IMAGE
    public void deleteImage(String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error deleting image: " + e.getMessage());
        }
    }

    // ✅ GET IMAGE AS RESOURCE
    public Resource getImageAsResource(String filename) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
        return new UrlResource(filePath.toUri());
    }
}