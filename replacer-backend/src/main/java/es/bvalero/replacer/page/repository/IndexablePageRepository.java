package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IndexablePageRepository {
    /** Find an indexable page including all the replacements */
    Optional<IndexablePageDB> findByPageId(IndexablePageId id);

    /** Find indexable pages, including all the replacements, by a range of page IDs. */
    List<IndexablePageDB> findByPageIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId);

    /** Reset the cache in case we are using the cache implementation */
    void resetCache(WikipediaLanguage lang);

    /** Update the title for a collection of indexable pages */
    void updatePageTitles(Collection<IndexablePageDB> pages);

    /** Insert a collection of pages without inserting the replacements */
    void insertPages(Collection<IndexablePageDB> pages);

    /** Insert a collection of replacements */
    void insertReplacements(Collection<IndexableReplacementDB> replacements);

    /** Update a collection of replacements */
    void updateReplacements(Collection<IndexableReplacementDB> replacements);

    /** Delete a collection of replacements */
    void deleteReplacements(Collection<IndexableReplacementDB> replacements);
}
