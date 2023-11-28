package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AccessToken;
import java.util.Collection;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Repository to perform operations on Wikipedia pages */
@SecondaryPort
public interface WikipediaPageRepository {
    Optional<WikipediaPage> findByTitle(WikipediaLanguage lang, String pageTitle);

    Optional<WikipediaPage> findByKey(PageKey pageKey);

    Collection<WikipediaPage> findByKeys(Collection<PageKey> pageKeys);

    Collection<WikipediaSection> findSectionsInPage(PageKey pageKey);

    Optional<WikipediaPage> findPageSection(WikipediaSection section);

    WikipediaSearchResult findByContent(WikipediaSearchRequest searchRequest);

    void save(WikipediaPageSave pageSave, AccessToken accessToken) throws WikipediaException;
}
