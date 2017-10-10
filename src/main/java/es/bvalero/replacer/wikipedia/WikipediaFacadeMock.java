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
    public String getArticleContent(String articleTitle) throws Exception {
        switch (articleTitle) {
            case MISSPELLING_LIST_ARTICLE:
                return loadArticleContent("/misspelling-list.txt");
            default:
                return loadArticleContent("/monkey-island.txt");
        }
    }

    public void editArticleContent(String articleTitle, String articleContent, String editSummary) {
    }

    private String loadArticleContent(String fileName) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(Paths.get(getClass().getResource(fileName).toURI())),
                StandardCharsets.UTF_8);
    }

}
