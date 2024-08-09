package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Repository to perform operations on Wikipedia pages */
@SecondaryPort
public interface WikipediaPageRepository {
    int MAX_SEARCH_RESULTS = 500; // MediaWiki API allows at most 500 pages in a search result

    Optional<WikipediaPage> findByTitle(WikipediaLanguage lang, String pageTitle);

    Optional<WikipediaPage> findByKey(PageKey pageKey);

    Collection<WikipediaPage> findByKeys(Collection<PageKey> pageKeys);

    Collection<WikipediaSection> findSectionsInPage(PageKey pageKey);

    Optional<WikipediaPage> findPageSection(WikipediaSection section);

    /** Find the IDs of the pages containing the given content */
    WikipediaSearchResult findByContent(WikipediaSearchRequest searchRequest);

    /** Find the pages containing the given content, using the default search options. */
    Collection<WikipediaPage> findByContent(WikipediaLanguage lang, String content);
}
