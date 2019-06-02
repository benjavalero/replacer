package es.bvalero.replacer.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
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
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

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
    public JsonNode executeOAuthRequest(Map<String, String> params, OAuth1AccessToken accessToken)
            throws AuthenticationException {
        OAuthRequest request = createOAuthRequestWithParams(params);
        signOAuthRequest(request, accessToken);
        return executeOAuthRequest(request);
    }

    @Override
    public JsonNode executeUnsignedOAuthRequest(Map<String, String> params) throws AuthenticationException {
        OAuthRequest request = createOAuthRequestWithParams(params);
        return executeOAuthRequest(request);
    }

    private OAuthRequest createOAuthRequestWithParams(Map<String, String> params) {
        OAuthRequest request = new OAuthRequest(Verb.POST, WIKIPEDIA_API_URL);
        addParametersToRequest(request, params);
        return request;
    }

    private void addParametersToRequest(OAuthRequest request, Map<String, String> params) {
        // Add standard parameters to receive a JSON response fro Wikipedia API
        request.addParameter("format", "json");
        request.addParameter("formatversion", "2");

        // Add the rest of parameters
        params.forEach(request::addParameter);
    }

    private void signOAuthRequest(OAuthRequest request, OAuth1AccessToken accessToken) {
        getOAuthService().signRequest(accessToken, request);
    }

    private JsonNode executeOAuthRequest(OAuthRequest request) throws AuthenticationException {
        try {
            Response response = getOAuthService().execute(request);
            if (response.isSuccessful() && response.getBody() != null) {
                return JSON_MAPPER.readTree(response.getBody());
            } else {
                throw new AuthenticationException(String.format("Call not successful: %d - %s", response.getCode(), response.getMessage()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthenticationException(e);
        } catch (ExecutionException | IOException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return getOAuthService().getAuthorizationUrl(requestToken);
    }

    @Override
    public OAuth1RequestToken getRequestToken() throws AuthenticationException {
        try {
            return getOAuthService().getRequestToken();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthenticationException(e);
        } catch (ExecutionException | IOException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
            throws AuthenticationException {
        try {
            return getOAuthService().getAccessToken(requestToken, oauthVerifier);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthenticationException(e);
        } catch (ExecutionException | IOException e) {
            throw new AuthenticationException(e);
        }
    }

}
