package es.bvalero.replacer.wikipedia;

import com.bitplan.mediawiki.japi.Mediawiki;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
@Profile("default")
public class WikipediaFacade implements IWikipediaFacade {

    public static final String MISSPELLING_LIST_ARTICLE = "Wikipedia:Corrector_ortográfico/Listado";
    private static final Logger LOGGER = LoggerFactory.getLogger(WikipediaFacade.class);
    private static final String WIKIPEDIA_URL = "https://es.wikipedia.org";
    private static final String WIKIPEDIA_API_URL = "https://es.wikipedia.org/w/api.php";

    @Autowired
    private HttpSession session;

    private Mediawiki wiki;

    @Value("${wikipedia.api.key}")
    private String apiKey;

    @Value("${wikipedia.api.secret}")
    private String apiSecret;

    private OAuth10aService oAuthService;

    public OAuth10aService getOAuthService() {
        if (oAuthService == null) {
            oAuthService = new ServiceBuilder(apiKey)
                    .apiSecret(apiSecret)
                    .callback("oob")
                    .build(MediaWikiApi.instance());
        }
        return oAuthService;
    }

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

    @Override
    public void editArticleContent(String articleTitle, String articleContent, String editSummary)
            throws WikipediaException {
        try {
            final OAuthRequest request = new OAuthRequest(Verb.POST, WIKIPEDIA_API_URL);
            request.addParameter("action", "edit");
            request.addParameter("title", articleTitle);
            request.addParameter("text", articleContent);
            request.addParameter("summary", editSummary);
            request.addParameter("minor", "true");
            // TODO ¿Hace falta un token de edición?

            OAuth1AccessToken accessToken = (OAuth1AccessToken) session.getAttribute("accessToken");
            getOAuthService().signRequest(accessToken, request);
            Response response = getOAuthService().execute(request);
            LOGGER.debug("Edit response: {}", response);
        } catch (Exception e) {
            LOGGER.error("Error saving content for article: " + articleTitle, e);
            throw new WikipediaException(e);
        }
    }

}
