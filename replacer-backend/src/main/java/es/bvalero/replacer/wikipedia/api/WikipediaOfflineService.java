package es.bvalero.replacer.wikipedia.api;

import es.bvalero.replacer.common.FileUtils;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("offline")
class WikipediaOfflineService implements WikipediaService {

    @Override
    public WikipediaUser getAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        return WikipediaUser.of("offline", List.of(WikipediaUserGroup.AUTOCONFIRMED), true);
    }

    @Override
    public boolean isAdminUser(String username) {
        return true;
    }

    @Override
    public Optional<WikipediaPage> getPageByTitle(WikipediaLanguage lang, String pageTitle) throws ReplacerException {
        return Optional.of(buildFakePage(1));
    }

    private WikipediaPage buildFakePage(int pageId) throws ReplacerException {
        LocalDateTime now = LocalDateTime.now();
        return WikipediaPage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
            .namespace(WikipediaNamespace.getDefault())
            .title("América del Norte")
            .content(FileUtils.getFileContent("/offline/sample-page.txt"))
            .lastUpdate(now)
            .queryTimestamp(now)
            .build();
    }

    @Override
    public Optional<WikipediaPage> getPageById(WikipediaPageId id) throws ReplacerException {
        return Optional.of(buildFakePage(id.getPageId()));
    }

    @Override
    public Collection<WikipediaSection> getPageSections(WikipediaPageId id) {
        return Collections.emptyList();
    }

    @Override
    public Optional<WikipediaPage> getPageSection(WikipediaPageId id, WikipediaSection section)
        throws ReplacerException {
        return Optional.of(buildFakePage(id.getPageId()));
    }

    @Override
    public WikipediaSearchResult searchByText(
        WikipediaLanguage lang,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) {
        return WikipediaSearchResult.of(1, Collections.singletonList(1));
    }

    @Override
    public void savePageContent(
        WikipediaPageId id,
        @Nullable Integer section,
        String pageContent,
        LocalDateTime queryTimestamp,
        String editSummary,
        AccessToken accessToken
    ) {
        // Do nothing
    }
}
