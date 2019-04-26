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

@Service
@Profile("offline")
class WikipediaFacadeMock implements IWikipediaFacade {

    @Override
    public String getPageContent(String pageTitle) throws WikipediaException {
        String content;
        switch (pageTitle) {
            case MISSPELLING_LIST_PAGE:
                content = loadArticleContent("/misspelling-list.txt");
                break;
            case FALSE_POSITIVE_LIST_PAGE:
                content = loadArticleContent("/false-positives.txt");
                break;
            default:
                content = loadArticleContent("/article-long.txt");
                break;
        }
        return content;
    }

    @Override
    public String getPageContent(String pageTitle, OAuth1AccessToken accessToken) {
        return null;
    }

    @Override
    public String getPageContent(int pageId, OAuth1AccessToken accessToken) {
        return null;
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

}
