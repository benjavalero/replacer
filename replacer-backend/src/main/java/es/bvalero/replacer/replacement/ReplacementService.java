package es.bvalero.replacer.replacement;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ReplacementService {

    @Autowired
    @Qualifier("replacementProxy")
    private ReplacementDao replacementDao;

    ///// CRUD

    public void insert(ReplacementEntity entity) {
        replacementDao.insert(entity);
    }

    public void insert(List<ReplacementEntity> entityList) {
        replacementDao.insert(entityList);
    }

    public void update(List<ReplacementEntity> entityList) {
        replacementDao.update(entityList);
    }

    public void updateDate(List<ReplacementEntity> entityList) {
        replacementDao.updateDate(entityList);
    }

    public void deleteAll(List<ReplacementEntity> entityList) {
        replacementDao.delete(entityList);
    }

    ///// INDEXATION

    public List<ReplacementEntity> findByPageInterval(int minPageId, int maxPageId, WikipediaLanguage lang) {
        return replacementDao.findByPageInterval(minPageId, maxPageId, lang);
    }

    public void deleteObsoleteByPageId(WikipediaLanguage lang, Set<Integer> pageIds) {
        replacementDao.deleteObsoleteByPageId(lang, pageIds);
    }

    public List<ReplacementEntity> findByPageId(int pageId, WikipediaLanguage lang) {
        return replacementDao.findByPageId(pageId, lang);
    }

    ///// PAGE REVIEW

    public long findRandomIdToBeReviewed(long chunkSize, WikipediaLanguage lang) {
        return replacementDao.findRandomIdToBeReviewed(chunkSize, lang);
    }

    public List<Integer> findPageIdsToBeReviewed(WikipediaLanguage lang, long start, Pageable pageable) {
        return replacementDao.findPageIdsToBeReviewed(lang, start, pageable);
    }

    public List<Integer> findRandomPageIdsToBeReviewedBySubtype(
        WikipediaLanguage lang,
        String type,
        String subtype,
        Pageable pageable
    ) {
        return replacementDao.findRandomPageIdsToBeReviewedBySubtype(lang, type, subtype, pageable);
    }

    public long countPagesToBeReviewedBySubtype(WikipediaLanguage lang, String type, String subtype) {
        return replacementDao.countPagesToBeReviewedBySubtype(lang, type, subtype);
    }

    public List<Integer> findPageIdsReviewedByCustomTypeAndSubtype(WikipediaLanguage lang, String subtype) {
        return replacementDao.findPageIdsReviewedByCustomTypeAndSubtype(lang, subtype);
    }

    public void reviewByPageId(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    ) {
        replacementDao.reviewByPageId(lang, pageId, type, subtype, reviewer);
    }

    ///// STATISTICS

    long countReplacementsReviewed(WikipediaLanguage lang) {
        return replacementDao.countReplacementsReviewed(lang);
    }

    public long countReplacementsNotReviewed(WikipediaLanguage lang) {
        return replacementDao.countReplacementsNotReviewed(lang);
    }

    List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return replacementDao.countReplacementsGroupedByReviewer(lang);
    }

    List<TypeCount> countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException {
        return replacementDao.countReplacementsGroupedByType(lang).getTypeCounts();
    }

    ///// PAGE LISTS

    public List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype) {
        return replacementDao.findPageTitlesToReviewBySubtype(lang, type, subtype);
    }

    public void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype) {
        replacementDao.reviewAsSystemBySubtype(lang, type, subtype);
    }

    ///// OTHER

    public void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes) {
        replacementDao.deleteToBeReviewedBySubtype(lang, type, subtypes);
    }
}
