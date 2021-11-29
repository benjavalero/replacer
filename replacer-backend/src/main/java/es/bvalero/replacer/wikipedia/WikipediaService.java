package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.Nullable;

/** Service to perform operations on Wikipedia */
public interface WikipediaService {
    WikipediaUser getAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) throws ReplacerException;

    boolean isAdminUser(String username);

    Optional<WikipediaPage> getPageByTitle(WikipediaLanguage lang, String pageTitle) throws ReplacerException;

    Optional<WikipediaPage> getPageById(WikipediaPageId id) throws ReplacerException;

    List<WikipediaSection> getPageSections(WikipediaPageId id) throws ReplacerException;

    Optional<WikipediaPage> getPageSection(WikipediaPageId id, WikipediaSection section) throws ReplacerException;

    WikipediaSearchResult searchByText(
        WikipediaLanguage lang,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) throws ReplacerException;

    void savePageContent(
        WikipediaPageId id,
        @Nullable Integer section,
        String pageContent,
        LocalDateTime queryTimestamp,
        String editSummary,
        AccessToken accessToken
    ) throws ReplacerException;
}
