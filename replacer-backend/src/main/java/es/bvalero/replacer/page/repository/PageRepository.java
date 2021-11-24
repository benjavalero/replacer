package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PageRepository {
    /** Find a page including all the replacements */
    Optional<PageModel> findByPageId(WikipediaPageId id);

    /** Find pages, including all the replacements, by a range of page IDs. */
    List<PageModel> findByPageIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId);

    /** Reset the cache in case we are using the cache implementation */
    void resetCache();

    /** Update the title for a collection of pages */
    void updatePageTitles(Collection<PageModel> pages);

    /** Insert a collection of pages without inserting the replacements */
    void insertPages(Collection<PageModel> pages);

    void deletePages(Collection<PageModel> pages);

    /** Insert a collection of replacements */
    void insertReplacements(Collection<ReplacementModel> replacements);

    /** Update a collection of replacements */
    void updateReplacements(Collection<ReplacementModel> replacements);

    /** Delete a collection of replacements */
    void deleteReplacements(Collection<ReplacementModel> replacements);
}
