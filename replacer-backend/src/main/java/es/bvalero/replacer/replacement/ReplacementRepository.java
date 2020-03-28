package es.bvalero.replacer.replacement;

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
public interface ReplacementRepository extends JpaRepository<ReplacementEntity, Long> {

    List<ReplacementEntity> findByArticleId(int articleId);

    @Query("SELECT new es.bvalero.replacer.replacement.TypeSubtypeCount(type, subtype, COUNT(*)) FROM ReplacementEntity WHERE reviewer IS NULL GROUP BY type, subtype")
    List<TypeSubtypeCount> countGroupedByTypeAndSubtype();

    @Query("SELECT articleId FROM ReplacementEntity WHERE reviewer IS NULL ORDER BY RAND()")
    List<Integer> findRandomArticleIdsToReview(Pageable pageable);

    @Query("SELECT articleId FROM ReplacementEntity WHERE type = :type AND subtype = :subtype AND reviewer IS NULL ORDER BY RAND()")
    List<Integer> findRandomArticleIdsToReviewByTypeAndSubtype(
            @Param("type") String type, @Param("subtype") String subtype, Pageable pageable);

    long countByArticleIdAndTypeAndSubtypeAndReviewerNotNull(int articleId, String type, String subtype);

    List<ReplacementEntity> findByArticleIdAndTypeAndSubtypeAndReviewerIsNull(int articleId, String type, String subtype);

    List<ReplacementEntity> findByArticleIdAndReviewerIsNull(int articleId);

    long countByReviewerIsNullOrReviewerIsNot(String reviewer);

    long countByReviewerIsNull();

    long countByReviewerIsNotNullAndReviewerIsNot(String reviewer);

    @Query("SELECT new es.bvalero.replacer.replacement.ReviewerCount(reviewer, COUNT(*)) FROM ReplacementEntity WHERE reviewer IS NOT NULL AND reviewer <> :systemReviewer GROUP BY reviewer ORDER BY COUNT(*) DESC")
    List<ReviewerCount> countGroupedByReviewer(String systemReviewer);

    void deleteBySubtypeIn(Set<String> subtypes);

}
