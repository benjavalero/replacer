package es.bvalero.replacer.wikipedia.api;

import es.bvalero.replacer.common.*;
import es.bvalero.replacer.wikipedia.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public UserInfo getUserInfo(WikipediaLanguage lang, OAuthToken accessToken) {
        return UserInfo.of("offline", List.of("autoconfirmed"));
    }

    @Override
    public Optional<WikipediaPage> getPageByTitle(String pageTitle, WikipediaLanguage lang) throws ReplacerException {
        return Optional.of(buildFakePage(1));
    }

    private WikipediaPage buildFakePage(int pageId) throws ReplacerException {
        LocalDateTime nowDate = LocalDateTime.now();
        String now = DateUtils.formatWikipediaTimestamp(nowDate);
        return WikipediaPage
            .builder()
            .id(pageId)
            .lang(WikipediaLanguage.getDefault())
            .namespace(WikipediaNamespace.ARTICLE)
            .title("América del Norte")
            .content(FileUtils.getFileContent("/offline/sample-page.txt"))
            .lastUpdate(nowDate.toLocalDate())
            .queryTimestamp(now)
            .build();
    }

    @Override
    public Optional<WikipediaPage> getPageById(int pageId, WikipediaLanguage lang) throws ReplacerException {
        return Optional.of(buildFakePage(pageId));
    }

    @Override
    public List<WikipediaPage> getPagesByIds(List<Integer> pageIds, WikipediaLanguage lang) throws ReplacerException {
        List<WikipediaPage> pages = new ArrayList<>();
        for (Integer id : pageIds) {
            getPageByTitle(Integer.toString(id), lang).ifPresent(pages::add);
        }
        return pages;
    }

    @Override
    public List<WikipediaSection> getPageSections(int pageId, WikipediaLanguage lang) {
        return Collections.emptyList();
    }

    @Override
    public Optional<WikipediaPage> getPageByIdAndSection(int pageId, WikipediaSection section, WikipediaLanguage lang)
        throws ReplacerException {
        return Optional.of(buildFakePage(pageId).withSection(section));
    }

    @Override
    public WikipediaSearchResult getPageIdsByStringMatch(
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
        WikipediaLanguage lang,
        int pageId,
        @Nullable Integer section,
        String pageContent,
        String currentTimestamp,
        String editSummary,
        OAuthToken accessToken
    ) {
        // Do nothing
    }
}
