package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import org.springframework.lang.Nullable;

interface PageRepository {
    /** Find a page including all the replacements */
    Optional<IndexedPage> findPageByKey(PageKey pageKey);

    /** Find pages, including all the replacements, by a range of page IDs. */
    Collection<IndexedPage> findPagesByIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId);

    /** Add a collection of pages without adding the replacements */
    void add(Collection<IndexedPage> pages);

    /** Update a collection of pages */
    void update(Collection<IndexedPage> pages);

    /** Update the date of the last update of the page */
    void updateLastUpdate(PageKey pageKey, LocalDate lastUpdate);

    /** Remove a collection of pages and its related replacements */
    void removeByKey(Collection<PageKey> pageKeys);

    /** Find a random batch of pages to review (optionally for a given type) and return the keys */
    Collection<PageKey> findNotReviewed(WikipediaLanguage lang, @Nullable StandardType type, int numResults);

    /** Find the pages to review by the given type and return the titles */
    Collection<String> findPageTitlesNotReviewedByType(WikipediaLanguage lang, StandardType type);
}
