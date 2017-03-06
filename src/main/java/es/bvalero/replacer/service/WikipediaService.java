package es.bvalero.replacer.service;

import com.bitplan.mediawiki.japi.Mediawiki;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WikipediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikipediaService.class);
    private static final String WIKIPEDIA_URL = "https://es.wikipedia.org";
    private Mediawiki wiki;

    @Value("${wikipedia.username}")
    private String username;

    @Value("${wikipedia.password}")
    private String password;

    public String getArticleContent(String articleTitle) {
        LOGGER.info("Getting content for article: {}", articleTitle);

        String content = null;
        try {
            content = getWiki().getPageContent(articleTitle);
        } catch (Exception e) {
            LOGGER.error("Error getting content for article: {}", articleTitle, e);
        }
        return content;
    }

    public void editArticleContent(String articleTitle, String articleContent, String editSummary) {
        try {
            login();
            getWiki().edit(articleTitle, articleContent, editSummary);
        } catch (Exception e) {
            LOGGER.error("Error saving content for article: {}", articleTitle, e);
        }
    }

    private void login() {
        try {
            getWiki().login(username, password);
        } catch (Exception e) {
            LOGGER.error("Error logging into Wikipedia", e);
        }
    }

    private Mediawiki getWiki() throws Exception {
        if (wiki == null) {
            wiki = new Mediawiki(WIKIPEDIA_URL);
        }
        return wiki;
    }

}
