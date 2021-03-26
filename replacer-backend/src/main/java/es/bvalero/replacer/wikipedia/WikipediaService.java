package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.Nullable;

public interface WikipediaService {
    RequestToken getRequestToken() throws ReplacerException;

    WikipediaUser getLoggedUser(String requestToken, String requestTokenSecret, String oauthVerifier)
        throws ReplacerException;

    String getMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException;

    String getFalsePositiveListPageContent(WikipediaLanguage lang) throws ReplacerException;

    String getComposedMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException;

    Optional<WikipediaPage> getPageByTitle(String pageTitle, WikipediaLanguage lang) throws ReplacerException;

    Optional<WikipediaPage> getPageById(int pageId, WikipediaLanguage lang) throws ReplacerException;

    List<WikipediaPage> getPagesByIds(List<Integer> pageIds, WikipediaLanguage lang) throws ReplacerException;

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
        AccessToken accessToken
    ) throws ReplacerException;
}
