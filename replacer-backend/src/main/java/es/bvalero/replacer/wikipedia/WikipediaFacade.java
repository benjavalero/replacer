package es.bvalero.replacer.wikipedia;

import com.bitplan.mediawiki.japi.Mediawiki;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.IAuthenticationService;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("default")
public class WikipediaFacade implements IWikipediaFacade {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(WikipediaFacade.class);
    private static final String WIKIPEDIA_URL = "https://es.wikipedia.org";
    private static final String EDIT_SUMMARY = "Correcciones ortográficas";

    @Autowired
    private IAuthenticationService authenticationService;

    private Mediawiki wiki;

    private Mediawiki getWiki() throws WikipediaException {
        if (wiki == null) {
            try {
                wiki = new Mediawiki(WIKIPEDIA_URL);
            } catch (Exception e) {
                throw new WikipediaException(e);
            }
        }
        return wiki;
    }

    @Override
    public String getArticleContent(String articleTitle) throws WikipediaException {
        LOGGER.info("Getting content for article: {}", articleTitle);
        try {
            String articleContent = getWiki().getPageContent(articleTitle);
            if (articleContent == null) {
                LOGGER.info("Article not available: {}", articleTitle);
                throw new UnavailableArticleException();
            }
            return articleContent;
        } catch (Exception e) {
            throw new WikipediaException(e);
        }
    }

    @Override
    public void editArticleContent(String articleTitle, String articleContent, OAuth1AccessToken accessToken)
            throws WikipediaException {
        // TODO : Check just before uploading there are no changes during the edition
        try {
            OAuthRequest request = authenticationService.createOauthRequest();
            request.addParameter("format", "json");
            request.addParameter("action", "edit");
            request.addParameter("title", articleTitle);
            request.addParameter("text", articleContent);
            request.addParameter("summary", EDIT_SUMMARY);
            request.addParameter("minor", "true");
            request.addParameter("token", authenticationService.getEditToken(accessToken));

            authenticationService.signAndExecuteOauthRequest(request, accessToken);
        } catch (AuthenticationException e) {
            throw new WikipediaException(e);
        }
    }

}
