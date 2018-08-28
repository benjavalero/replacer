package es.bvalero.replacer.article;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for potential errors in database.
 */
@Repository
@Transactional
public interface PotentialErrorRepository extends JpaRepository<PotentialError, Long> {

    @Query("SELECT COUNT(*) FROM PotentialError WHERE type = 'MISSPELLING' AND article.reviewDate IS NULL")
    Long countNotReviewed();

    @Query("SELECT text, COUNT(*) FROM PotentialError WHERE type = 'MISSPELLING' AND article.reviewDate IS NULL GROUP BY text")
    List<Object[]> findMisspellingsGrouped();

    @Query(value = "SELECT pe.article FROM PotentialError pe WHERE pe.text = :word AND pe.article.reviewDate IS NULL ORDER BY RAND()")
    List<Article> findRandomByWordAndReviewDateNull(@Param("word") String word, Pageable pageable);

}