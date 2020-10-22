package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.ReplacerException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("offline")
public class WikipediaServiceOfflineImpl implements WikipediaService {

    @Override
    public String getLoggedUserName(OAuth1AccessToken accessToken, WikipediaLanguage lang) {
        return "offline";
    }

    @Override
    public boolean isAdminUser(String username) {
        return true;
    }

    @Override
    public String getMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return loadPageContent("/es/bvalero/replacer/wikipedia/misspelling-list.txt");
    }

    @Override
    public String getFalsePositiveListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return loadPageContent("/es/bvalero/replacer/wikipedia/false-positives.txt");
    }

    @Override
    public String getComposedMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return loadPageContent("/es/bvalero/replacer/wikipedia/composed-misspellings.txt");
    }

    @Override
    public Optional<WikipediaPage> getPageByTitle(String pageTitle, WikipediaLanguage lang) throws ReplacerException {
        return Optional.of(buildFakePage(1));
    }

    private WikipediaPage buildFakePage(int pageId) throws ReplacerException {
        LocalDateTime nowDate = LocalDateTime.now();
        String now = WikipediaPage.formatWikipediaTimestamp(nowDate);
        return WikipediaPage
            .builder()
            .id(pageId)
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .title("América del Norte")
            .content(loadPageContent("/es/bvalero/replacer/wikipedia/article-long.txt"))
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
    public Optional<WikipediaPage> getPageByIdAndSection(int pageId, int section, WikipediaLanguage lang)
        throws ReplacerException {
        return Optional.of(buildFakePage(pageId).withSection(section));
    }

    @Override
    public PageSearchResult getPageIdsByStringMatch(String text, int offset, int limit, WikipediaLanguage lang) {
        return new PageSearchResult(1, Collections.singletonList(1));
    }

    @Override
    public void savePageContent(
        int pageId,
        String pageContent,
        @Nullable Integer section,
        String currentTimestamp,
        WikipediaLanguage lang,
        OAuth1AccessToken accessToken
    ) {
        // Do nothing
    }

    private String loadPageContent(String fileName) throws ReplacerException {
        LOGGER.info("Load fake content from file: {}", fileName);
        try {
            return Files.readString(Paths.get(getClass().getResource(fileName).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new ReplacerException(e);
        }
    }
}
