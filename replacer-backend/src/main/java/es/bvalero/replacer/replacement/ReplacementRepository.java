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
    List<ReplacementEntity> findByPageIdAndLang(int pageId, String lang);

    @Query(
        "SELECT DISTINCT(title) FROM ReplacementEntity " +
        "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL"
    )
    List<String> findPageTitlesByTypeAndSubtype(
        @Param("lang") String lang,
        @Param("type") String type,
        @Param("subtype") String subtype
    );

    @Query(
        "SELECT FLOOR(MIN(id) + (MAX(id) - MIN(id) + 1 - :chunkSize) * RAND()) " +
        "FROM ReplacementEntity " +
        "WHERE lang = :lang AND reviewer IS NULL"
    )
    long findRandomStart(@Param("chunkSize") long chunkSize, @Param("lang") String lang);

    // ORDER BY RAND() takes a lot when not filtering by type/subtype even using an index
    // Not worth to DISTINCT as we add the results as a set later
    @Query(
        "SELECT pageId " +
        "FROM ReplacementEntity " +
        "WHERE lang = :lang AND reviewer IS NULL AND id > :start " +
        "ORDER BY id"
    )
    List<Integer> findRandomPageIdsToReview(
        @Param("lang") String lang,
        @Param("start") long randomStart,
        Pageable pageable
    );

    @Query(
        "SELECT COUNT (DISTINCT pageId) FROM ReplacementEntity " +
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
        "SELECT pageId FROM ReplacementEntity " +
        "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL " +
        "ORDER BY RAND()"
    )
    List<Integer> findRandomPageIdsToReviewByTypeAndSubtype(
        @Param("lang") String lang,
        @Param("type") String type,
        @Param("subtype") String subtype,
        Pageable pageable
    );

    @Query(
        "SELECT pageId FROM ReplacementEntity " +
        "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NOT NULL"
    )
    List<Integer> findByLangAndTypeAndSubtypeAndReviewerNotNull(
        @Param("lang") String lang,
        @Param("type") String type,
        @Param("subtype") String subtype
    );

    List<ReplacementEntity> findByPageIdAndLangAndTypeAndSubtypeAndReviewerIsNull(
        int pageId,
        String lang,
        String type,
        String subtype
    );

    List<ReplacementEntity> findByPageIdAndLangAndReviewerIsNull(int pageId, String lang);

    void deleteByLangAndTypeAndSubtypeInAndReviewerIsNull(String lang, String type, Set<String> subtypes);
}
