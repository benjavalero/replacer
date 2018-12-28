package es.bvalero.replacer.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for articles in database.
 */
@Repository
@Transactional
public interface ArticleRepository extends JpaRepository<Article, Integer>, ArticleRepositoryCustom {

    Article findByTitle(String title);

    @Query("SELECT COUNT(*) FROM Article AS a WHERE NOT EXISTS (FROM Replacement AS r WHERE r.article = a)")
    Long countReviewed();

    @Query("SELECT COUNT(*) FROM Article AS a WHERE EXISTS (FROM Replacement AS r WHERE r.article = a)")
    Long countNotReviewed();

    List<Article> findByIdGreaterThanOrderById(Integer minId, Pageable pageable);

}