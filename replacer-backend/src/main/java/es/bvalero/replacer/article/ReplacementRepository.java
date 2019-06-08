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

    List<Replacement> findByArticleIdAndStatus(int articleId, ReplacementStatus status);

    void deleteByArticleIdIn(Set<Integer> articleIds);

    @Query("SELECT new es.bvalero.replacer.article.ArticleTimestamp(articleId, MAX(lastUpdate)) FROM Replacement WHERE articleId BETWEEN :minId AND :maxId GROUP BY articleId")
    List<ArticleTimestamp> findMaxLastUpdate(@Param("minId") int minArticleId, @Param("maxId") int maxArticleId);

    void deleteByArticleIdAndStatus(int articleId, ReplacementStatus status);

    @Query("SELECT new es.bvalero.replacer.article.ReplacementCount(subtype, COUNT(*)) FROM Replacement WHERE type = 'MISSPELLING' GROUP BY subtype")
    List<ReplacementCount> findMisspellingsGrouped();

    @Query("FROM Replacement WHERE status = :status ORDER BY RAND()")
    List<Replacement> findRandomByStatus(@Param("status") ReplacementStatus status, Pageable pageable);

    @Query("FROM Replacement WHERE subtype = :word AND status = :status ORDER BY RAND()")
    List<Replacement> findRandomByWordAndStatus(@Param("word") String word, @Param("status") ReplacementStatus status,
                                                Pageable pageable);

    long countByStatus(ReplacementStatus status);

    long countByStatusIn(Set<ReplacementStatus> status);

}