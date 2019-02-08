package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Profile("offline")
class WikipediaFacadeMock implements IWikipediaFacade {

    @Override
    public boolean isAuthenticated(HttpServletRequest request) {
        return true;
    }

    @Override
    public OAuth10aService getOAuthService() {
        return null;
    }

    @Override
    public String getArticleContent(String articleTitle) throws WikipediaException {
        String content;
        switch (articleTitle) {
            case MISSPELLING_LIST_ARTICLE:
                content = loadArticleContent("/misspelling-list.txt");
                break;
            case FALSE_POSITIVE_LIST_ARTICLE:
                content = loadArticleContent("/false-positives.txt");
                break;
            default:
                content = loadArticleContent("/article-long.txt");
                break;
        }
        return content;
    }

    @Override
    public void editArticleContent(String articleTitle, String articleContent) {
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
