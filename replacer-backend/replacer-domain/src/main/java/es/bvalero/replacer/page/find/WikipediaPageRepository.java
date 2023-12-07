package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Repository to perform operations on Wikipedia pages */
@SecondaryPort
public interface WikipediaPageRepository {
    Optional<WikipediaPage> findByTitle(WikipediaLanguage lang, String pageTitle);

    Optional<WikipediaPage> findByKey(PageKey pageKey);

    Collection<WikipediaSection> findSectionsInPage(PageKey pageKey);

    Optional<WikipediaPage> findPageSection(WikipediaSection section);

    WikipediaSearchResult findByContent(WikipediaSearchRequest searchRequest);
}
