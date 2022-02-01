package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.Nullable;

/** Service to perform operations on Wikipedia */
public interface WikipediaService {
    WikipediaUser getAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) throws WikipediaException;

    WikipediaUser getWikipediaUser(WikipediaLanguage lang, String username) throws WikipediaException;

    Optional<WikipediaPage> getPageByTitle(WikipediaLanguage lang, String pageTitle);

    Optional<WikipediaPage> getPageById(WikipediaPageId id);

    Collection<WikipediaPage> getPagesByIds(WikipediaLanguage lang, List<Integer> pageIds);

    Collection<WikipediaSection> getPageSections(WikipediaPageId id) throws WikipediaException;

    Optional<WikipediaPage> getPageSection(WikipediaPageId id, WikipediaSection section);

    WikipediaSearchResult searchByText(
        WikipediaLanguage lang,
        Collection<WikipediaNamespace> namespaces,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) throws WikipediaException;

    void savePageContent(
        WikipediaPageId id,
        @Nullable Integer section,
        String pageContent,
        LocalDateTime queryTimestamp,
        String editSummary,
        AccessToken accessToken
    ) throws WikipediaException;
}
