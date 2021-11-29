package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import java.util.Collection;
import java.util.Optional;

public interface PageRepository {
    /** Find a page including all the replacements */
    Optional<PageModel> findByPageId(WikipediaPageId id);

    /** Find pages, including all the replacements, by a range of page IDs. */
    Collection<PageModel> findByPageIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId);

    /** Update the title for a collection of pages */
    void updatePageTitles(Collection<PageModel> pages);

    /** Insert a collection of pages without inserting the replacements */
    void insertPages(Collection<PageModel> pages);

    void deletePages(Collection<PageModel> pages);
}
