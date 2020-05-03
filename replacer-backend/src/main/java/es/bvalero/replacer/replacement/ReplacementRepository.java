package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for replacements in database.
 */
@Repository
@Transactional
public interface ReplacementRepository extends JpaRepository<ReplacementEntity, Long> {
    List<ReplacementEntity> findByArticleIdAndLang(int articleId, WikipediaLanguage lang);

    @Query("SELECT new es.bvalero.replacer.replacement.TypeSubtypeCount(type, subtype, COUNT(*)) FROM ReplacementEntity WHERE lang = :lang AND reviewer IS NULL GROUP BY type, subtype")
    List<TypeSubtypeCount> countGroupedByTypeAndSubtype(@Param("lang") String lang);

    @Query("SELECT articleId FROM ReplacementEntity WHERE lang = :lang AND reviewer IS NULL ORDER BY RAND()")
    List<Integer> findRandomArticleIdsToReview(@Param("lang") String lang, Pageable pageable);

    @Query("SELECT articleId FROM ReplacementEntity WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL ORDER BY RAND()")
    List<Integer> findRandomArticleIdsToReviewByTypeAndSubtype(
        @Param("lang") String lang,
        @Param("type") String type,
        @Param("subtype") String subtype,
        Pageable pageable
    );

    long countByArticleIdAndLangAndTypeAndSubtypeAndReviewerNotNull(int articleId, String lang, String type, String subtype);

    List<ReplacementEntity> findByArticleIdAndLangAndTypeAndSubtypeAndReviewerIsNull(
        int articleId,
        String lang,
        String type,
        String subtype
    );

    List<ReplacementEntity> findByArticleIdAndLangAndReviewerIsNull(int articleId, String lang);

    long countByReviewerIsNullOrReviewerIsNot(String reviewer);

    long countByReviewerIsNull();

    long countByReviewerIsNotNullAndReviewerIsNot(String reviewer);

    @Query("SELECT new es.bvalero.replacer.replacement.ReviewerCount(reviewer, COUNT(*)) FROM ReplacementEntity WHERE reviewer IS NOT NULL AND reviewer <> :systemReviewer GROUP BY reviewer ORDER BY COUNT(*) DESC")
    List<ReviewerCount> countGroupedByReviewer(String systemReviewer);

    void deleteByLangAndSubtypeIn(String lang, Set<String> subtypes);
}
