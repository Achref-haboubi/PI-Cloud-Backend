package tn.esprit.peakwell.repositories;

import tn.esprit.peakwell.entities.Article;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByAuthor(String author);
    List<Article> findByTitleContaining(String title);
}
