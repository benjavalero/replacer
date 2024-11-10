package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.Collection;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Repository to perform operations on Wikipedia pages */
@SecondaryPort
public interface WikipediaPageRepository {
    int MAX_PAGES_REQUESTED = 50; // MediaWiki API allows to retrieve the content of maximum 50 pages
    int MAX_SEARCH_RESULTS = 500; // MediaWiki API allows at most 500 pages in a search result
    int MAX_OFFSET_LIMIT = 10000; // MediaWiki API has a maximum offset of 10000 when searching

    Optional<WikipediaPage> findByTitle(WikipediaLanguage lang, String pageTitle);

    Optional<WikipediaPage> findByKey(PageKey pageKey);

    Collection<WikipediaPage> findByKeys(Collection<PageKey> pageKeys) throws WikipediaException;

    Collection<WikipediaSection> findSectionsInPage(PageKey pageKey);

    Optional<WikipediaPage> findPageSection(WikipediaSection section);

    /** Find the IDs of the pages containing the given content */
    WikipediaSearchResult findByContent(WikipediaSearchRequest searchRequest);
}
