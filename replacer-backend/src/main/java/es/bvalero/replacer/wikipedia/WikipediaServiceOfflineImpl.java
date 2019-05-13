package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
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

@Service
@Profile("offline")
class WikipediaServiceOfflineImpl implements WikipediaService {

    @Override
    public WikipediaPage getPageByTitle(String pageTitle) throws WikipediaException {
        return WikipediaPage.builder()
                .setContent(loadArticleContent("/article-long.txt"))
                .build();
    }

    @Override
    public Map<Integer, WikipediaPage> getPagesByIds(List<Integer> pageIds) throws WikipediaException {
        Map<Integer, WikipediaPage> pagesContent = new HashMap<>();
        for (Integer id : pageIds) {
            pagesContent.put(id, getPageByTitle(""));
        }
        return pagesContent;
    }

    @Override
    public void savePageContent(String pageTitle, String pageContent, LocalDateTime editTime, OAuth1AccessToken accessToken) {
        // Do nothing
    }

    private String loadArticleContent(String fileName) throws WikipediaException {
        try {
            return new String(Files.readAllBytes(Paths.get(getClass().getResource(fileName).toURI())),
                    StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new WikipediaException(e);
        }
    }

    public String getMisspellingListPageContent() throws WikipediaException {
        return loadArticleContent("/misspelling-list.txt");
    }

    public String getFalsePositiveListPageContent() throws WikipediaException {
        return loadArticleContent("/false-positives.txt");
    }

}
