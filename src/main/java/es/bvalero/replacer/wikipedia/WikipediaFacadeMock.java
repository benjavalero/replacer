package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Profile("offline")
class WikipediaFacadeMock implements IWikipediaFacade {

    @Autowired
    private HttpSession session;

    @Override
    public OAuth10aService getOAuthService() {
        session.setAttribute("accessToken", "");
        return null;
    }

    @Override
    public String getArticleContent(String articleTitle) throws WikipediaException {
        if (WikipediaFacade.MISSPELLING_LIST_ARTICLE.equals(articleTitle)) {
            return loadArticleContent("/misspelling-list.txt");
        } else {
            return loadArticleContent("/article-long.txt");
        }
    }

    @Override
    public void editArticleContent(String articleTitle, String articleContent, String editSummary) {
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
