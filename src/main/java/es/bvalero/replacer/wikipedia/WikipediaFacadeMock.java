package es.bvalero.replacer.wikipedia;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Profile("offline")
class WikipediaFacadeMock implements IWikipediaFacade {

    @Override
    public String getArticleContent(String articleTitle) throws WikipediaException {
        if (WikipediaFacade.MISSPELLING_LIST_ARTICLE.equals(articleTitle)) {
            return loadArticleContent("/misspelling-list.txt");
        } else {
            return loadArticleContent("/monkey-island.txt");
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
