package es.bvalero.replacer.wikipedia.api;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.util.FileOfflineUtils;
import es.bvalero.replacer.wikipedia.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
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
        return WikipediaUser.builder().name("offline").group(WikipediaUserGroup.AUTO_CONFIRMED).build();
    }

    @Override
    public Optional<WikipediaPage> getPageByTitle(WikipediaLanguage lang, String pageTitle) {
        return buildFakePage(1);
    }

    private Optional<WikipediaPage> buildFakePage(int pageId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            return Optional.of(
                WikipediaPage
                    .builder()
                    .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
                    .namespace(WikipediaNamespace.getDefault())
                    .title("América del Norte")
                    .content(FileOfflineUtils.getFileContent("/offline/sample-page.txt"))
                    .lastUpdate(now)
                    .queryTimestamp(now)
                    .build()
            );
        } catch (ReplacerException e) {
            LOGGER.error("Error building fake page", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<WikipediaPage> getPageById(WikipediaPageId id) {
        return buildFakePage(id.getPageId());
    }

    @Override
    public Collection<WikipediaSection> getPageSections(WikipediaPageId id) {
        return Collections.emptyList();
    }

    @Override
    public Optional<WikipediaPage> getPageSection(WikipediaPageId id, WikipediaSection section) {
        return buildFakePage(id.getPageId());
    }

    @Override
    public WikipediaSearchResult searchByText(
        WikipediaLanguage lang,
        Collection<WikipediaNamespace> namespaces,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) {
        return WikipediaSearchResult.builder().total(1).pageId(1).build();
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
