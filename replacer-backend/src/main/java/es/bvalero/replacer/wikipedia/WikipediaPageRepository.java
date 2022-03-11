package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.lang.Nullable;

/** Repository to perform operations on Wikipedia pages */
public interface WikipediaPageRepository {
    Optional<WikipediaPage> findByTitle(WikipediaLanguage lang, String pageTitle);

    Optional<WikipediaPage> findById(WikipediaPageId id);

    Collection<WikipediaPage> findByIds(WikipediaLanguage lang, Collection<Integer> pageIds);

    Collection<WikipediaSection> findSectionsInPage(WikipediaPageId id);

    Optional<WikipediaPage> findPageSection(WikipediaPageId id, WikipediaSection section);

    WikipediaSearchResult findByContent(
        WikipediaLanguage lang,
        Collection<WikipediaNamespace> namespaces,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    );

    void save(
        WikipediaPageId id,
        @Nullable Integer section,
        String content,
        LocalDateTime queryTimestamp,
        String editSummary,
        AccessToken accessToken
    ) throws WikipediaException;
}
