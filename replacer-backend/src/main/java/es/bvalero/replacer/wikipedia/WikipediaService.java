package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.Nullable;

/** Service to perform operations on Wikipedia */
public interface WikipediaService {
    WikipediaUser getAuthenticatedUser(WikipediaLanguage lang, OAuthToken accessToken) throws ReplacerException;

    Optional<WikipediaPage> getPageByTitle(WikipediaLanguage lang, String pageTitle) throws ReplacerException;

    Optional<WikipediaPage> getPageById(WikipediaLanguage lang, int pageId) throws ReplacerException;

    List<WikipediaSection> getPageSections(WikipediaLanguage lang, int pageId) throws ReplacerException;

    Optional<WikipediaPage> getPageSection(WikipediaLanguage lang, int pageId, WikipediaSection section)
        throws ReplacerException;

    WikipediaSearchResult searchByText(
        WikipediaLanguage lang,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) throws ReplacerException;

    void savePageContent(
        WikipediaLanguage lang,
        int pageId,
        @Nullable Integer section,
        String pageContent,
        LocalDateTime queryTimestamp,
        String editSummary,
        OAuthToken accessToken
    ) throws ReplacerException;
}
