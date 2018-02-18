package es.bvalero.replacer.wikipedia;

import com.bitplan.mediawiki.japi.Mediawiki;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("default")
public class WikipediaFacade implements IWikipediaFacade {

    public static final String MISSPELLING_LIST_ARTICLE = "Wikipedia:Corrector_ortográfico/Listado";
    private static final Logger LOGGER = LoggerFactory.getLogger(WikipediaFacade.class);
    private static final String WIKIPEDIA_URL = "https://es.wikipedia.org";

    private Mediawiki wiki;

    @Value("${wikipedia.username}")
    private String username;

    @Value("${wikipedia.password}")
    private String password;

    @Override
    public String getArticleContent(String articleTitle) throws WikipediaException {
        LOGGER.info("Getting content for article: {}", articleTitle);
        try {
            String articleContent = getWiki().getPageContent(articleTitle);
            if (articleContent == null) {
                throw new WikipediaException("Article not available");
            }
            return articleContent;
        } catch (Exception e) {
            throw new WikipediaException(e);
        }
    }

    private Mediawiki getWiki() throws WikipediaException {
        if (this.wiki == null) {
            try {
                this.wiki = new Mediawiki(WIKIPEDIA_URL);
            } catch (Exception e) {
                throw new WikipediaException(e);
            }
        }
        return this.wiki;
    }

    public void editArticleContent(String articleTitle, String articleContent, String editSummary)
            throws WikipediaException {
        try {
            login();
            getWiki().edit(articleTitle, articleContent, editSummary);
        } catch (Exception e) {
            LOGGER.error("Error saving content for article: " + articleTitle, e);
            throw new WikipediaException(e);
        }
    }

    private void login() throws WikipediaException {
        try {
            getWiki().login(username, password);
        } catch (Exception e) {
            LOGGER.error("Error logging into Wikipedia", e);
            throw new WikipediaException(e);
        }
    }

}
