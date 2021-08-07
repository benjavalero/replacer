package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.Nullable;

/** Service to perform operations on Wikipedia */
public interface WikipediaService {
    UserInfo getUserInfo(WikipediaLanguage lang, OAuthToken accessToken) throws ReplacerException;

    Optional<WikipediaPage> getPageByTitle(WikipediaLanguage lang, String pageTitle) throws ReplacerException;

    Optional<WikipediaPage> getPageById(WikipediaLanguage lang, int pageId) throws ReplacerException;

    List<WikipediaSection> getPageSections(WikipediaLanguage lang, int pageId) throws ReplacerException;

    Optional<WikipediaPage> getPageByIdAndSection(WikipediaLanguage lang, int pageId, WikipediaSection section)
        throws ReplacerException;

    WikipediaSearchResult getPageIdsByStringMatch(
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
        String currentTimestamp,
        String editSummary,
        OAuthToken accessToken
    ) throws ReplacerException;
}
