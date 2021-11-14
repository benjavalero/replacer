package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
import java.util.List;
import java.util.Optional;

public interface IndexablePageRepository {
    /** Find an indexable page including all the replacements */
    Optional<IndexablePageDB> findByPageId(IndexablePageId id);

    List<IndexablePageDB> findByPageIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId);

    /** Reset the cache in case we are using the cache implementation */
    void resetCache(WikipediaLanguage lang);
}
