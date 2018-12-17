package es.bvalero.replacer.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for replacements in database.
 */
@Repository
@Transactional
public interface ReplacementRepository extends JpaRepository<Replacement, Long> {

    List<Replacement> findByArticle(Article article);

    void deleteByArticle(Article article);

    void deleteByArticleAndText(Article article, String text);

    @Query("SELECT new es.bvalero.replacer.persistence.ReplacementCount(text, COUNT(*)) FROM Replacement WHERE type = 'MISSPELLING' GROUP BY text")
    List<ReplacementCount> findMisspellingsGrouped();

    @Query("SELECT pe.article FROM Replacement pe WHERE pe.text = :word ORDER BY RAND()")
    List<Article> findRandomByWord(@Param("word") String word, Pageable pageable);

}