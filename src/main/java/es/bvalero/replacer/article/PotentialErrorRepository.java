package es.bvalero.replacer.article;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface PotentialErrorRepository extends JpaRepository<PotentialError, Long> {

    /* TODO Remove this query if not necessary
    public Integer countMisspellings() {
        Query query = getEntityManager().createQuery("SELECT COUNT(*) FROM ReplacementBD WHERE lastReviewed IS NULL");
        return ((Long) query.getSingleResult()).intValue();
    }
*/
    @Query("SELECT text, COUNT(*) FROM PotentialError WHERE type = 'MISSPELLING' GROUP BY text")
    List<Object[]> findMisspellingsGrouped();

}