package es.bvalero.replacer.wikipedia.page;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.util.FileOfflineUtils;
import es.bvalero.replacer.wikipedia.*;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("offline")
class WikipediaPageOfflineRepository implements WikipediaPageRepository {

    @Override
    public Optional<WikipediaPage> findByTitle(WikipediaLanguage lang, String pageTitle) {
        return buildFakePage(1);
    }

    private Optional<WikipediaPage> buildFakePage(int pageId) {
        try {
            return Optional.of(
                WikipediaPage.builder()
                    .pageKey(PageKey.of(WikipediaLanguage.SPANISH, pageId))
                    .namespace(WikipediaNamespace.ARTICLE)
                    .title("América del Norte")
                    .content(FileOfflineUtils.getFileContent("offline/sample-page.txt"))
                    .lastUpdate(WikipediaTimestamp.now())
                    .queryTimestamp(WikipediaTimestamp.now())
                    .build()
            );
        } catch (ReplacerException e) {
            LOGGER.error("Error building fake page", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<WikipediaPage> findByKey(PageKey pageKey, AccessToken accessToken) {
        return buildFakePage(pageKey.getPageId());
    }

    @Override
    public Stream<WikipediaPage> findByKeys(Collection<PageKey> pageKeys, @Nullable AccessToken accessToken) {
        return pageKeys
            .stream()
            .map(pageKey -> buildFakePage(pageKey.getPageId()))
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    @Override
    public Collection<WikipediaSection> findSectionsInPage(PageKey pageKey, AccessToken accessToken) {
        return List.of();
    }

    @Override
    public Optional<WikipediaPage> findPageSection(WikipediaSection section, AccessToken accessToken) {
        return buildFakePage(section.getPageKey().getPageId());
    }

    @Override
    public WikipediaSearchResult findByContent(
        WikipediaSearchRequest searchRequest,
        @Nullable AccessToken accessToken
    ) {
        return WikipediaSearchResult.builder().total(1).pageId(1).build();
    }
}
