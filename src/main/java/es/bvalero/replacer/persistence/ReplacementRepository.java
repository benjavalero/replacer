package es.bvalero.replacer.persistence;

import es.bvalero.replacer.article.MisspellingCount;
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
public interface ReplacementRepository extends JpaRepository<Replacement, Long> {

    List<Replacement> findByArticle(Article article);

    void deleteByArticle(Article article);

    @Query("SELECT new es.bvalero.replacer.article.MisspellingCount(text, COUNT(*)) FROM Replacement WHERE type = 'MISSPELLING' GROUP BY text")
    List<MisspellingCount> findMisspellingsGrouped();

    @Query("SELECT pe.article FROM Replacement pe WHERE pe.text = :word ORDER BY RAND()")
    List<Article> findRandomByWord(@Param("word") String word, Pageable pageable);

}