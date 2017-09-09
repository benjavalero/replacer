package es.bvalero.replacer.persistence;

import es.bvalero.replacer.domain.Count;
import es.bvalero.replacer.persistence.pojo.ReplacementDbPk;
import es.bvalero.replacer.persistence.pojo.ReplacementDb;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Repository
@Transactional
public class ReplacementDao extends AbstractDao<ReplacementDbPk, ReplacementDb> {

    private Integer numReplacements = null;

    public ReplacementDao() {
        super();
    }

    public void deleteReplacementsByTitle(String title) {
        Query query = getEntityManager().createQuery("DELETE FROM ReplacementBD WHERE title = :title");
        query.setParameter("title", title);
        query.executeUpdate();
    }

    public List<ReplacementDb> findAllReviewedReplacements() {
        TypedQuery<ReplacementDb> query = getEntityManager()
                .createQuery("FROM ReplacementBD WHERE dtfixed IS NOT NULL", ReplacementDb.class);
        return query.getResultList();
    }

    private Integer countReplacements() {
        Query query = getEntityManager().createQuery("SELECT COUNT(*) FROM ReplacementBD");
        return ((Long) query.getSingleResult()).intValue();
    }

    public ReplacementDb findRandomReplacementToFix() {
        Random randomGenerator = new Random();
        int startRow = randomGenerator.nextInt(getNumReplacements());
        Query query = getEntityManager().createQuery("FROM ReplacementBD WHERE dtfixed IS NULL");
        query.setFirstResult(startRow);
        query.setMaxResults(1);
        List results = query.getResultList();
        return results.isEmpty() ? null : (ReplacementDb) query.getResultList().get(0);
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

    public void setArticleAsReviewed(String title) {
        Query query = getEntityManager().createQuery("UPDATE ReplacementBD SET lastReviewed = :now WHERE title = :title");
        query.setParameter("title", title);
        query.setParameter("now", new Date());
        query.executeUpdate();
    }

    public Integer countMisspellings() {
        Query query = getEntityManager().createQuery("SELECT COUNT(*) FROM ReplacementBD WHERE lastReviewed IS NULL");
        return ((Long) query.getSingleResult()).intValue();
    }

    public Integer countArticles() {
        Query query = getEntityManager().createQuery("SELECT COUNT(DISTINCT title) FROM ReplacementBD WHERE lastReviewed IS NULL");
        return ((Long) query.getSingleResult()).intValue();
    }

    public List<Count> findMisspellingsGrouped() {
        List<Count> misspellingCount = new ArrayList<>();
        Query query = getEntityManager().createQuery("SELECT word, COUNT(*) FROM ReplacementBD WHERE lastReviewed IS NULL GROUP BY word ORDER BY COUNT(*) DESC");
        List<Object[]> results = query.getResultList();
        for (Object[] result : results) {
            misspellingCount.add(new Count((String) result[0], (Long) result[1]));
        }
        return misspellingCount;
    }

}