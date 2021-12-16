package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import java.util.Collection;
import java.util.Optional;

public interface PageIndexRepository {
    /** Find a page including all the replacements */
    Optional<PageModel> findPageById(WikipediaPageId id);

    /** Find pages, including all the replacements, by a range of page IDs. */
    Collection<PageModel> findPagesByIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId);
}
