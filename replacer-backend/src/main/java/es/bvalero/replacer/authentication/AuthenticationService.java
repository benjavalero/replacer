package es.bvalero.replacer.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Service
@Profile("default")
class AuthenticationService implements IAuthenticationService {

    private static final String WIKIPEDIA_API_URL = "https://es.wikipedia.org/w/api.php";
    private static final String TOKEN_ACCESS = "accessToken";
    private static final String TOKEN_REQUEST = "requestToken";

    @Autowired
    private HttpSession session;

    @Value("${wikipedia.api.key}")
    private String apiKey;

    @Value("${wikipedia.api.secret}")
    private String apiSecret;

    private OAuth10aService oAuthService;

    private OAuth10aService getOAuthService() {
        if (oAuthService == null) {
            oAuthService = new ServiceBuilder(apiKey)
                    .apiSecret(apiSecret)
                    .callback("oob")
                    .build(MediaWikiApi.instance());
        }
        return oAuthService;
    }

    @Override
    public OAuthRequest createOauthRequest() {
        return new OAuthRequest(Verb.POST, WIKIPEDIA_API_URL);
    }

    @Override
    public Response signAndExecuteOauthRequest(OAuthRequest request) throws AuthenticationException {
        OAuth1AccessToken accessToken = getAccessTokenInSession();
        if (accessToken == null) {
            throw new AuthenticationException("Null Access Token");
        } else {
            try {
                getOAuthService().signRequest(getAccessTokenInSession(), request);
                Response response = getOAuthService().execute(request);
                checkOauthResponse(response);
                return response;
            } catch (Exception e) {
                throw new AuthenticationException(e);
            }
        }
    }

    private void checkOauthResponse(Response response) throws AuthenticationException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonError = mapper.readTree(response.getBody()).get("error");
            if (jsonError != null) {
                String errMsg = "[error: " + jsonError.get("code") + ", info: " + jsonError.get("info") + ']';
                throw new AuthenticationException(errMsg);
            }
        } catch (IOException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public String getEditToken() throws AuthenticationException {
        try {
            OAuthRequest request = createOauthRequest();
            request.addParameter("format", "json");
            request.addParameter("action", "query");
            request.addParameter("meta", "tokens");

            Response response = signAndExecuteOauthRequest(request);
            checkOauthResponse(response);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());
            return json.get("query").get("tokens").get("csrftoken").asText();
        } catch (IOException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public boolean isAuthenticated() {
        // Check if the access token exists
        return getAccessTokenInSession() != null;
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return getOAuthService().getAuthorizationUrl(requestToken);
    }

    /* REQUEST TOKEN */

    @Override
    public OAuth1RequestToken getRequestToken() throws InterruptedException, ExecutionException, IOException {
        return getOAuthService().getRequestToken();
    }

    @Override
    public OAuth1RequestToken getRequestTokenInSession() {
        Object requestToken = session.getAttribute(TOKEN_REQUEST);
        if (requestToken == null) {
            return null;
        } else {
            return (OAuth1RequestToken) requestToken;
        }
    }

    @Override
    public void setRequestTokenInSession(OAuth1RequestToken requestToken) {
        session.setAttribute(TOKEN_REQUEST, requestToken);
    }

    @Override
    public void removeRequestTokenInSession() {
        session.removeAttribute(TOKEN_REQUEST);
    }

    /* ACCESS TOKEN */

    @Override
    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
            throws InterruptedException, ExecutionException, IOException {
        return getOAuthService().getAccessToken(requestToken, oauthVerifier);
    }

    private OAuth1AccessToken getAccessTokenInSession() {
        Object accessToken = session.getAttribute(TOKEN_ACCESS);
        if (accessToken == null) {
            return null;
        } else {
            return (OAuth1AccessToken) accessToken;
        }
    }

    @Override
    public void setAccessTokenInSession(OAuth1AccessToken accessToken) {
        session.setAttribute(TOKEN_ACCESS, accessToken);
    }

}
