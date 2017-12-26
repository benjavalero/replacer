package es.bvalero.replacer.article;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    @Query("SELECT MAX(id) FROM Article WHERE reviewDate IS NULL")
    Integer findMaxId();

    Article findFirstByIdGreaterThanAndReviewDateNull(Integer minId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Article SET reviewDate = NOW() WHERE id = :id")
    void setArticleAsReviewed(@Param("id") Integer id);

    Long countByReviewDateNull();

    Long countByReviewDateNotNull();

}