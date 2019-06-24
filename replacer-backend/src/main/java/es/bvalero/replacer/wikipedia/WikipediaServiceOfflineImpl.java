package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Profile("offline")
class WikipediaServiceOfflineImpl implements WikipediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikipediaServiceOfflineImpl.class);

    @Override
    public Optional<WikipediaPage> getPageByTitle(String pageTitle) throws WikipediaException {
        String now = WikipediaPage.formatWikipediaTimestamp(LocalDateTime.now());
        return Optional.of(WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setTitle("Norteamérica")
                .setContent(loadArticleContent("/article-long.txt"))
                .setTimestamp(now)
                .setQueryTimestamp(now)
                .build());
    }

    @Override
    public Optional<WikipediaPage> getPageById(int articleId) throws WikipediaException {
        return getPageByTitle("");
    }

    @Override
    public Map<Integer, WikipediaPage> getPagesByIds(List<Integer> pageIds) throws WikipediaException {
        Map<Integer, WikipediaPage> pages = new HashMap<>();
        for (Integer id : pageIds) {
            getPageByTitle("").ifPresent(page -> pages.put(id, page));
        }
        return pages;
    }

    @Override
    public void savePageContent(int pageId, String pageContent, String currentTimestamp, OAuth1AccessToken accessToken) {
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

    @Override
    public String getMisspellingListPageContent() throws WikipediaException {
        return loadArticleContent("/misspelling-list.txt");
    }

    @Override
    public String getFalsePositiveListPageContent() throws WikipediaException {
        return loadArticleContent("/false-positives.txt");
    }

    @Override
    public String identify(OAuth1AccessToken accessToken) {
        return "offline";
    }

}
