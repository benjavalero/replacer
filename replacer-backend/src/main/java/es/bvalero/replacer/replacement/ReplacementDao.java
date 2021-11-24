package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;

public interface ReplacementDao {
    ///// PAGE REVIEW

    long findRandomIdToBeReviewed(WikipediaLanguage lang, long chunkSize);

    List<Integer> findPageIdsToBeReviewed(WikipediaLanguage lang, long start, Pageable pageable);

    List<Integer> findRandomPageIdsToBeReviewedBySubtype(
        WikipediaLanguage lang,
        String type,
        String subtype,
        Pageable pageable
    );

    long countPagesToBeReviewedBySubtype(WikipediaLanguage lang, String type, String subtype);

    ///// PAGE LISTS

    List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype);

    ///// MISSPELLING MANAGER

    void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes);
}
