package es.bvalero.replacer.replacement;

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
    List<ReplacementEntity> findByArticleIdAndLang(int articleId, String lang);

    @Query(
        "SELECT new es.bvalero.replacer.replacement.TypeSubtypeCount(lang, type, subtype, COUNT(*)) " +
        "FROM ReplacementEntity " +
        "WHERE reviewer IS NULL " +
        "GROUP BY lang, type, subtype"
    )
    List<TypeSubtypeCount> countGroupedByTypeAndSubtype();

    @Query(
        "SELECT FLOOR(MIN(id) + (MAX(id) - MIN(id) + 1 - :chunkSize) * RAND()) " +
        "FROM ReplacementEntity " +
        "WHERE lang = :lang AND reviewer IS NULL"
    )
    long findRandomStart(@Param("chunkSize") long chunkSize, @Param("lang") String lang);

    // Not worth to DISTINCT. Besides this count is also used in statistics.
    long countByLangAndReviewerIsNull(String lang);

    // ORDER BY RAND() takes a lot when not filtering by type/subtype even using an index
    // Not worth to DISTINCT as we add the results as a set later
    @Query(
        "SELECT articleId " +
        "FROM ReplacementEntity " +
        "WHERE lang = :lang AND reviewer IS NULL AND id > :start " +
        "ORDER BY id"
    )
    List<Integer> findRandomArticleIdsToReview(
        @Param("lang") String lang,
        @Param("start") long randomStart,
        Pageable pageable
    );

    @Query(
        "SELECT COUNT (DISTINCT articleId) FROM ReplacementEntity " +
        "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL"
    )
    long countByLangAndTypeAndSubtypeAndReviewerIsNull(
        @Param("lang") String lang,
        @Param("type") String type,
        @Param("subtype") String subtype
    );

    // When filtering by type/subtype ORDER BY RAND() still takes a while but it is admissible
    // Not worth to DISTINCT as we add the results as a set later
    @Query(
        "SELECT articleId FROM ReplacementEntity " +
        "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL " +
        "ORDER BY RAND()"
    )
    List<Integer> findRandomArticleIdsToReviewByTypeAndSubtype(
        @Param("lang") String lang,
        @Param("type") String type,
        @Param("subtype") String subtype,
        Pageable pageable
    );

    @Query(
        "SELECT articleId FROM ReplacementEntity " +
        "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NOT NULL"
    )
    List<Integer> findByLangAndTypeAndSubtypeAndReviewerNotNull(
        @Param("lang") String lang,
        @Param("type") String type,
        @Param("subtype") String subtype
    );

    List<ReplacementEntity> findByArticleIdAndLangAndTypeAndSubtypeAndReviewerIsNull(
        int articleId,
        String lang,
        String type,
        String subtype
    );

    List<ReplacementEntity> findByArticleIdAndLangAndReviewerIsNull(int articleId, String lang);

    long countByLangAndReviewerIsNotNullAndReviewerIsNot(String lang, String reviewer);

    @Query(
        "SELECT new es.bvalero.replacer.replacement.ReviewerCount(reviewer, COUNT(*)) " +
        "FROM ReplacementEntity " +
        "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :systemReviewer " +
        "GROUP BY reviewer " +
        "ORDER BY COUNT(*) DESC"
    )
    List<ReviewerCount> countGroupedByReviewer(
        @Param("lang") String lang,
        @Param("systemReviewer") String systemReviewer
    );

    void deleteByLangAndTypeAndSubtypeInAndReviewerIsNull(String lang, String type, Set<String> subtypes);
}
