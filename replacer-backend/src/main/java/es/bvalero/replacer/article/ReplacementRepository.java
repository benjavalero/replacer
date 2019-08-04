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

    @Query("FROM Replacement WHERE articleId BETWEEN :minId AND :maxId")
    List<Replacement> findByArticles(@Param("minId") int minArticleId, @Param("maxId") int maxArticleId);

    @Query("SELECT new es.bvalero.replacer.article.ReplacementCount(type, subtype, COUNT(*)) FROM Replacement WHERE reviewer IS NULL GROUP BY type, subtype")
    List<ReplacementCount> findReplacementCountByTypeAndSubtype();

    @Query("FROM Replacement WHERE reviewer IS NULL ORDER BY RAND()")
    List<Replacement> findRandomToReview(Pageable pageable);

    @Query("FROM Replacement WHERE type = :type AND subtype = :subtype AND reviewer IS NULL ORDER BY RAND()")
    List<Replacement> findRandomToReviewByTypeAndSubtype(
            @Param("type") String type, @Param("subtype") String subtype, Pageable pageable);

    List<Replacement> findByArticleIdAndTypeAndSubtypeAndReviewerNotNull(int articleId, String type, String subtype);

    List<Replacement> findByArticleIdAndTypeAndSubtypeAndReviewerIsNull(int articleId, String type, String subtype);

    List<Replacement> findByArticleIdAndReviewerIsNull(int articleId);

    long countByReviewerIsNullOrReviewerIsNot(String reviewer);

    long countByReviewerIsNull();

    long countByReviewerIsNotNullAndReviewerIsNot(String reviewer);

    @Query("SELECT reviewer, COUNT(*) FROM Replacement WHERE reviewer <> :systemReviewer GROUP BY reviewer ORDER BY COUNT(*) DESC")
    List<Object[]> countGroupedByReviewer(String systemReviewer);

    void deleteBySubtypeIn(Set<String> subtypes);

}