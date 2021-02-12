package es.bvalero.replacer.replacement;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ReplacementService {

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private ReplacementDao replacementDao;

    @Autowired
    private ReplacementStatsDao replacementStatsDao;

    ///// CRUD

    public List<ReplacementEntity> findByPageId(int pageId, WikipediaLanguage lang) {
        return replacementDao.findByPageId(pageId, lang);
    }

    public void insert(ReplacementEntity entity) {
        replacementDao.insert(entity);
    }

    ///// DUMP INDEXATION

    public List<ReplacementEntity> findByPageInterval(int minPageId, int maxPageId, WikipediaLanguage lang) {
        return replacementDao.findByPageInterval(minPageId, maxPageId, lang);
    }

    public void indexObsoleteByPageId(WikipediaLanguage lang, int pageId) {
        IndexablePage dummyPage = WikipediaPage.builder().lang(lang).id(pageId).lastUpdate(LocalDate.now()).build();
        replacementIndexService.indexPageReplacements(dummyPage, Collections.emptyList());
    }

    ///// PAGE REVIEW

    public long findRandomIdToBeReviewed(WikipediaLanguage lang, long chunkSize) {
        return replacementDao.findRandomIdToBeReviewed(lang, chunkSize);
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
        return replacementStatsDao.countReplacementsReviewed(lang);
    }

    public long countReplacementsNotReviewed(WikipediaLanguage lang) {
        return replacementStatsDao.countReplacementsNotReviewed(lang);
    }

    List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return replacementStatsDao.countReplacementsGroupedByReviewer(lang);
    }

    List<TypeCount> countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException {
        return replacementStatsDao.countReplacementsGroupedByType(lang).getTypeCounts();
    }

    ///// PAGE LISTS

    public List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype) {
        return replacementDao.findPageTitlesToReviewBySubtype(lang, type, subtype);
    }

    public void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype) {
        replacementDao.reviewAsSystemBySubtype(lang, type, subtype);
    }

    ///// MISSPELLING MANAGER

    public void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes) {
        replacementDao.deleteToBeReviewedBySubtype(lang, type, subtypes);
    }
}
