package es.bvalero.replacer.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for articles in database.
 */
@Repository
@Transactional
public interface ArticleRepository extends JpaRepository<Article, Integer>, ArticleRepositoryCustom {

    @Query("FROM Article WHERE reviewDate IS NULL ORDER BY RAND()")
    List<Article> findRandomArticleNotReviewed(Pageable pageable);

    Article findByTitle(String title);

    void deleteByTitle(String title);

    Long countByReviewDateNull();

    Long countByReviewDateNotNull();

    List<Article> findByIdGreaterThanOrderById(Integer minId, Pageable pageable);

}