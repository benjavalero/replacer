package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
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
class WikipediaServiceOfflineImpl implements WikipediaService {

    private static final String AUTHORIZATION_URL = "/?oauth_verifier=x";

    @Override
    public RequestToken getRequestToken() {
        return RequestToken.of("", "", AUTHORIZATION_URL);
    }

    @Override
    public WikipediaUser getLoggedUser(String requestToken, String requestTokenSecret, String oauthVerifier) {
        return WikipediaUser.of("offline", true, AccessToken.ofEmpty());
    }

    @Override
    public String getMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return WikipediaUtils.getFileContent("/offline/misspelling-list.txt");
    }

    @Override
    public String getFalsePositiveListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return WikipediaUtils.getFileContent("/offline/false-positives.txt");
    }

    @Override
    public String getComposedMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return WikipediaUtils.getFileContent("/offline/composed-misspellings.txt");
    }

    @Override
    public Optional<WikipediaPage> getPageByTitle(String pageTitle, WikipediaLanguage lang) throws ReplacerException {
        return Optional.of(buildFakePage(1));
    }

    private WikipediaPage buildFakePage(int pageId) throws ReplacerException {
        LocalDateTime nowDate = LocalDateTime.now();
        String now = WikipediaUtils.formatWikipediaTimestamp(nowDate);
        return WikipediaPage
            .builder()
            .id(pageId)
            .lang(WikipediaLanguage.getDefault())
            .namespace(WikipediaNamespace.ARTICLE)
            .title("América del Norte")
            .content(WikipediaUtils.getFileContent("/offline/sample-article.txt"))
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
        return Optional.of(buildFakePage(pageId).withSection(section.getIndex()));
    }

    @Override
    public PageSearchResult getPageIdsByStringMatch(
        WikipediaLanguage lang,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) {
        return PageSearchResult.of(1, Collections.singletonList(1));
    }

    @Override
    public void savePageContent(
        WikipediaLanguage lang,
        int pageId,
        @Nullable Integer section,
        String pageContent,
        String currentTimestamp,
        AccessToken accessToken
    ) {
        // Do nothing
    }
}
