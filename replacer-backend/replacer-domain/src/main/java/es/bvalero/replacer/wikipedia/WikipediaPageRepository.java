package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AccessToken;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.lang.Nullable;

/** Repository to perform operations on Wikipedia pages */
public interface WikipediaPageRepository {
    Optional<WikipediaPage> findByTitle(WikipediaLanguage lang, String pageTitle);

    Optional<WikipediaPage> findByKey(PageKey pageKey);

    Collection<WikipediaPage> findByKeys(Collection<PageKey> pageKeys);

    Collection<WikipediaSection> findSectionsInPage(PageKey pageKey);

    Optional<WikipediaPage> findPageSection(PageKey pageKey, WikipediaSection section);

    WikipediaSearchResult findByContent(
        WikipediaLanguage lang,
        Collection<WikipediaNamespace> namespaces,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    );

    void save(
        PageKey pageKey,
        @Nullable Integer section,
        String content,
        LocalDateTime queryTimestamp,
        String editSummary,
        AccessToken accessToken
    ) throws WikipediaException;
}
