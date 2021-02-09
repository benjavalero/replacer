package es.bvalero.replacer.replacement;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

interface ReplacementDao {
    ///// CRUD

    void insert(ReplacementEntity entity);

    void insert(List<ReplacementEntity> entityList);

    void update(ReplacementEntity entity);

    void update(List<ReplacementEntity> entityList);

    void updateDate(List<ReplacementEntity> entityList);

    void deleteAll(List<ReplacementEntity> entityList);

    ///// INDEXATION

    List<ReplacementEntity> findByPageInterval(int minPageId, int maxPageId, WikipediaLanguage lang);

    void deleteObsoleteByPageId(WikipediaLanguage lang, Set<Integer> pageIds);

    List<ReplacementEntity> findByPageId(int pageId, WikipediaLanguage lang);

    ///// PAGE REVIEW

    long findRandomIdToBeReviewed(long chunkSize, WikipediaLanguage lang);

    List<Integer> findPageIdsToBeReviewed(WikipediaLanguage lang, long start, Pageable pageable);

    List<Integer> findRandomPageIdsToBeReviewedBySubtype(
        WikipediaLanguage lang,
        String type,
        String subtype,
        Pageable pageable
    );

    long countPagesToBeReviewedBySubtype(WikipediaLanguage lang, String type, String subtype);

    List<Integer> findPageIdsReviewedByCustomTypeAndSubtype(WikipediaLanguage lang, String subtype);

    void reviewByPageId(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    );

    ///// STATISTICS

    long countReplacementsReviewed(WikipediaLanguage lang);

    long countReplacementsNotReviewed(WikipediaLanguage lang);

    List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang);

    LanguageCount countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException;

    ///// PAGE LISTS

    List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype);

    void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype);

    ///// OTHER

    void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes);
}
