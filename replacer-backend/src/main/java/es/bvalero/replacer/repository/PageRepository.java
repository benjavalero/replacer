package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import java.time.LocalDate;
import java.util.Collection;

public interface PageRepository {
    /** Add a collection of pages without adding the replacements */
    void addPages(Collection<PageModel> pages);

    /** Update a collection of pages */
    void updatePages(Collection<PageModel> pages);

    /** Update the date of the last update of the page */
    void updatePageLastUpdate(WikipediaPageId id, LocalDate lastUpdate);

    /** Remove a collection of pages and its related replacements */
    void removePagesById(Collection<WikipediaPageId> pages);

    /** Find a random batch of pages to review and return the IDs */
    Collection<Integer> findPageIdsToReview(WikipediaLanguage lang, int numResult);

    /** Count the number of pages to review */
    long countPagesToReview(WikipediaLanguage lang);

    /** Find a random batch of pages to review for a given type and return the IDs */
    Collection<Integer> findPageIdsToReviewByType(WikipediaLanguage lang, ReplacementType type, int numResult);

    /** Count the number of pages to review by type */
    long countPagesToReviewByType(WikipediaLanguage lang, ReplacementType type);

    /** Find the pages to review by the given type and return the titles */
    Collection<String> findPageTitlesToReviewByType(WikipediaLanguage lang, ReplacementType type);
}
