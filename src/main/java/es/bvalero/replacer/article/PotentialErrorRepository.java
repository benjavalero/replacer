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

    List<PotentialError> findByArticle(Article article);

    void deleteByArticle(Article article);

    @Query("SELECT text, COUNT(*) FROM PotentialError WHERE type = 'MISSPELLING' GROUP BY text")
    List<Object[]> findMisspellingsGrouped();

    @Query(value = "SELECT pe.article FROM PotentialError pe WHERE pe.text = :word ORDER BY RAND()")
    List<Article> findRandomByWord(@Param("word") String word, Pageable pageable);

}