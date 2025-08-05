package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.util.FileOfflineUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
    public Optional<WikipediaPage> findByKey(PageKey pageKey) {
        return buildFakePage(pageKey.getPageId());
    }

    @Override
    public Collection<WikipediaPage> findByKeys(Collection<PageKey> pageKeys) {
        return pageKeys.stream().map(this::findByKey).filter(Optional::isPresent).map(Optional::get).toList();
    }

    @Override
    public Collection<WikipediaSection> findSectionsInPage(PageKey pageKey) {
        return List.of();
    }

    @Override
    public Optional<WikipediaPage> findPageSection(WikipediaSection section) {
        return buildFakePage(section.getPageKey().getPageId());
    }

    @Override
    public WikipediaSearchResult findByContent(WikipediaSearchRequest searchRequest) {
        return WikipediaSearchResult.builder().total(1).pageId(1).build();
    }
}
