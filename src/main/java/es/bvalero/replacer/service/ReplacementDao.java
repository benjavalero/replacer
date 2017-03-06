package es.bvalero.replacer.service;

import es.bvalero.replacer.domain.ReplacementBD;
import es.bvalero.replacer.domain.ReplacementPK;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Repository
@Transactional
class ReplacementDao extends AbstractDao<ReplacementPK, ReplacementBD> {

    private Integer numReplacements = null;

    public ReplacementDao() {
        super();
    }

    void deleteReplacementsByTitle(String title) {
        Query query = getEntityManager().createQuery("DELETE FROM ReplacementBD WHERE title = :title");
        query.setParameter("title", title);
        query.executeUpdate();
    }

    List<ReplacementBD> findAllReviewedReplacements() {
        Query query = getEntityManager().createQuery("FROM ReplacementBD WHERE dtfixed IS NOT NULL");
        return query.getResultList();
    }

    private Integer countReplacements() {
        Query query = getEntityManager().createQuery("SELECT COUNT(*) FROM ReplacementBD");
        return ((Long) query.getSingleResult()).intValue();
    }

    ReplacementBD findRandomReplacementToFix() {
        Random randomGenerator = new Random();
        int startRow = randomGenerator.nextInt(getNumReplacements());
        Query query = getEntityManager().createQuery("FROM ReplacementBD WHERE dtfixed IS NULL");
        query.setFirstResult(startRow);
        query.setMaxResults(1);
        List results = query.getResultList();
        return results.isEmpty() ? null : (ReplacementBD) query.getResultList().get(0);
    }

    private Integer getNumReplacements() {
        if (numReplacements == null) {
            numReplacements = countReplacements();
        }
        return numReplacements;
    }

    // TODO Update when finish updating replacements from dump
    public void setNumReplacements(Integer numReplacements) {
        this.numReplacements = numReplacements;
    }

    void setArticleAsReviewed(String title) {
        Query query = getEntityManager().createQuery("UPDATE ReplacementBD SET lastReviewed = :now WHERE title = :title");
        query.setParameter("title", title);
        query.setParameter("now", new Date());
        query.executeUpdate();
    }

}