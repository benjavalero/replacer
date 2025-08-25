package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Repository to perform operations on Wikipedia pages */
@SecondaryPort
public interface WikipediaPageRepository {
    int MAX_PAGES_REQUESTED = 50; // MediaWiki API allows to retrieve the content of maximum 50 pages
    int MAX_SEARCH_RESULTS = 500; // MediaWiki API allows at most 500 pages in a search result
    int MAX_OFFSET_LIMIT = 10000; // MediaWiki API has a maximum offset of 10000 when searching

    Optional<WikipediaPage> findByTitle(WikipediaLanguage lang, String pageTitle);

    Optional<WikipediaPage> findByKey(PageKey pageKey, AccessToken accessToken);

    default Stream<WikipediaPage> findByKeys(Collection<PageKey> pageKeys) throws WikipediaException {
        return findByKeys(pageKeys, null);
    }

    Stream<WikipediaPage> findByKeys(Collection<PageKey> pageKeys, @Nullable AccessToken accessToken)
        throws WikipediaException;

    Collection<WikipediaSection> findSectionsInPage(PageKey pageKey, AccessToken accessToken);

    Optional<WikipediaPage> findPageSection(WikipediaSection section, AccessToken accessToken);

    /** Find the IDs of the pages containing the given content */
    default WikipediaSearchResult findByContent(WikipediaSearchRequest searchRequest) {
        return findByContent(searchRequest, null);
    }

    WikipediaSearchResult findByContent(WikipediaSearchRequest searchRequest, @Nullable AccessToken accessToken);
}
