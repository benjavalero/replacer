package es.bvalero.replacer.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.jetbrains.annotations.Nullable;
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
    public JsonNode executeOAuthRequest(Map<String, String> params, @Nullable OAuth1AccessToken accessToken)
            throws AuthenticationException {
        // Create the OAuth request
        OAuthRequest request = new OAuthRequest(Verb.POST, WIKIPEDIA_API_URL);

        // Add standard parameters to receive a JSON response fro Wikipedia API
        request.addParameter("format", "json");
        request.addParameter("formatversion", "2");

        // Add the rest of parameters
        params.forEach(request::addParameter);

        // Sign the request with the OAuth token
        if (accessToken != null) {
            getOAuthService().signRequest(accessToken, request);
        }

        // Execute the OAuth request
        try {
            Response response = getOAuthService().execute(request);
            if (response.isSuccessful() && response.getBody() != null) {
                return JSON_MAPPER.readTree(response.getBody());
            } else {
                throw new AuthenticationException(String.format("Call not successful: %d - %s", response.getCode(), response.getMessage()));
            }
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
