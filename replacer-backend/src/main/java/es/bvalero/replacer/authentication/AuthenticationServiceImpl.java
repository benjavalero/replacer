package es.bvalero.replacer.authentication;

import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// We make this implementation public to be used by the finder benchmarks
@Service
@Profile("default")
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String WIKIPEDIA_API_URL = "https://es.wikipedia.org/w/api.php";

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

    private OAuthRequest createOauthRequest() {
        OAuthRequest request = new OAuthRequest(Verb.POST, WIKIPEDIA_API_URL);
        request.addParameter("format", "json");
        request.addParameter("formatversion", "2");
        return request;
    }

    @Override
    public String executeOAuthRequest(Map<String, String> params) throws AuthenticationException {
        return executeAndSignOAuthRequest(params, null);
    }

    @Override
    public String executeAndSignOAuthRequest(Map<String, String> params, OAuth1AccessToken accessToken)
            throws AuthenticationException {
        try {
            OAuthRequest request = createOauthRequest();
            params.forEach(request::addParameter);
            if (accessToken != null) {
                getOAuthService().signRequest(accessToken, request);
            }
            return getOAuthService().execute(request).getBody();
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return getOAuthService().getAuthorizationUrl(requestToken);
    }

    @Override
    public OAuth1RequestToken getRequestToken() throws InterruptedException, ExecutionException, IOException {
        return getOAuthService().getRequestToken();
    }

    @Override
    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
            throws InterruptedException, ExecutionException, IOException {
        return getOAuthService().getAccessToken(requestToken, oauthVerifier);
    }

}
