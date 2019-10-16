package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@Profile("offline")
class WikipediaServiceOfflineImpl implements WikipediaService {

    @Override
    public String getLoggedUserName(OAuth1AccessToken accessToken) {
        return "offline";
    }

    @Override
    public boolean isAdminUser(String username) {
        return true;
    }

    @Override
    public String getMisspellingListPageContent() throws WikipediaException {
        return loadArticleContent("/es/bvalero/replacer/wikipedia/misspelling-list.txt");
    }

    @Override
    public String getFalsePositiveListPageContent() throws WikipediaException {
        return loadArticleContent("/es/bvalero/replacer/wikipedia/false-positives.txt");
    }

    @Override
    public String getComposedMisspellingListPageContent() throws WikipediaException {
        return loadArticleContent("/es/bvalero/replacer/wikipedia/composed-misspellings.txt");
    }

    @Override
    public Optional<WikipediaPage> getPageByTitle(String pageTitle) throws WikipediaException {
        return Optional.of(buildFakePage(1));
    }

    private WikipediaPage buildFakePage(int pageId) throws WikipediaException {
        LocalDate nowDate = LocalDate.now();
        String now = WikipediaPage.formatWikipediaTimestamp(nowDate);
        return WikipediaPage.builder()
                .id(pageId)
                .namespace(WikipediaNamespace.ARTICLE)
                .title("América del Norte")
                .content(loadArticleContent("/es/bvalero/replacer/wikipedia/article-long.txt"))
                .lastUpdate(nowDate)
                .queryTimestamp(now)
                .build();
    }

    @Override
    public Optional<WikipediaPage> getPageById(int articleId) throws WikipediaException {
        return getPageByIdAndSection(articleId, 0);
    }

    @Override
    public List<WikipediaPage> getPagesByIds(List<Integer> pageIds) throws WikipediaException {
        List<WikipediaPage> pages = new ArrayList<>();
        for (Integer id : pageIds) {
            getPageByTitle(Integer.toString(id)).ifPresent(pages::add);
        }
        return pages;
    }

    @Override
    public List<WikipediaSection> getPageSections(int pageId) {
        return Collections.emptyList();
    }

    @Override
    public Optional<WikipediaPage> getPageByIdAndSection(int pageId, int section) throws WikipediaException {
        return Optional.of(buildFakePage(pageId));
    }

    @Override
    public Set<Integer> getPageIdsByStringMatch(String text) {
        return Collections.singleton(1);
    }

    @Override
    public void savePageContent(int pageId, String pageContent, @Nullable Integer section, String currentTimestamp,
                                OAuth1AccessToken accessToken) {
        // Do nothing
    }

    private String loadArticleContent(String fileName) throws WikipediaException {
        LOGGER.info("Load fake content from file: {}", fileName);
        try {
            return new String(Files.readAllBytes(Paths.get(getClass().getResource(fileName).toURI())),
                    StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new WikipediaException(e);
        }
    }

}
