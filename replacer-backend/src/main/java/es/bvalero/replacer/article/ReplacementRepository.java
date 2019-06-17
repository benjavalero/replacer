package es.bvalero.replacer.article;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Repository for replacements in database.
 */
@Repository
@Transactional
interface ReplacementRepository extends JpaRepository<Replacement, Long>, ReplacementRepositoryCustom {

    List<Replacement> findByArticleId(int articleId);

    void deleteByArticleId(int articleId);

    void deleteByArticleIdIn(Set<Integer> articleIds);

    @Query("FROM Replacement WHERE articleId BETWEEN :minId AND :maxId")
    List<Replacement> findByArticles(@Param("minId") int minArticleId, @Param("maxId") int maxArticleId);

    @Query("SELECT new es.bvalero.replacer.article.ReplacementCount(subtype, COUNT(*)) FROM Replacement WHERE type = 'MISSPELLING' GROUP BY subtype")
    List<ReplacementCount> findMisspellingsGrouped();

    @Query("FROM Replacement WHERE reviewer IS NULL ORDER BY RAND()")
    List<Replacement> findRandomToReview(Pageable pageable);

    @Query("FROM Replacement WHERE subtype = :word AND reviewer IS NULL ORDER BY RAND()")
    List<Replacement> findRandomByWordToReview(@Param("word") String word, Pageable pageable);

    long countByReviewerIsNull();

    long countByReviewerIsNotNull();

    void deleteBySubtypeIn(Set<String> subtypes);

}