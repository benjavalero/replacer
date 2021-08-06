package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.Nullable;

/** Service to perform operations in Wikipedia */
public interface WikipediaService {
    UserInfo getUserInfo(WikipediaLanguage lang, OAuthToken accessToken) throws ReplacerException;

    Optional<WikipediaPage> getPageByTitle(String pageTitle, WikipediaLanguage lang) throws ReplacerException;

    Optional<WikipediaPage> getPageById(int pageId, WikipediaLanguage lang) throws ReplacerException;

    List<WikipediaSection> getPageSections(int pageId, WikipediaLanguage lang) throws ReplacerException;

    Optional<WikipediaPage> getPageByIdAndSection(int pageId, WikipediaSection section, WikipediaLanguage lang)
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
