package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;

public interface PageReviewRepository {
    /** Find a collection of pages to review. We only need the page IDs. */
    Collection<Integer> findToReview(WikipediaLanguage lang, int numResult);

    long countToReview(WikipediaLanguage lang);

    /** Find a collection of pages to review for a give type. We only need the page IDs. */
    Collection<Integer> findToReviewByType(WikipediaLanguage lang, String type, String subtype, int numResult);

    long countToReviewByType(WikipediaLanguage lang, String type, String subtype);
}
