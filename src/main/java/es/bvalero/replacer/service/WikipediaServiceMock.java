package es.bvalero.replacer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Profile("offline")
public class WikipediaServiceMock implements IWikipediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikipediaServiceMock.class);

    public String getArticleContent(String articleTitle) {
        LOGGER.info("Getting content for article: {}", articleTitle);

        switch (articleTitle) {
            case MisspellingService.MISSPELLING_LIST_ARTICLE:
                return loadArticleContent("/misspelling-list.txt");
            default:
                return loadArticleContent("/macbeth.txt");
        }
    }

    public void editArticleContent(String articleTitle, String articleContent, String editSummary) {
        // Do nothing
    }

    private String loadArticleContent(String fileName) {
        String fileString = null;

        try {
            fileString = new String(Files.readAllBytes(Paths.get(getClass().getResource(fileName).toURI())), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Error loading mock article content from: " + fileName, e);
        }

        return fileString;
    }

}
