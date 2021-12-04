package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import java.util.Collection;
import java.util.Optional;

public interface PageRepository {
    /** Find a page including all the replacements */
    Optional<PageModel> findByPageId(WikipediaPageId id);

    /** Find pages, including all the replacements, by a range of page IDs. */
    Collection<PageModel> findByPageIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId);

    /** Insert a collection of pages without inserting the replacements */
    void insertPages(Collection<PageModel> pages);

    /** Update a collection of pages. Actually only the title is updated. */
    void updatePages(Collection<PageModel> pages);

    /** Delete a collection of pages and its related replacements */
    void deletePages(Collection<PageModel> pages);

    Collection<String> findPageTitlesToReviewByType(WikipediaLanguage lang, String type, String subtype);
}
