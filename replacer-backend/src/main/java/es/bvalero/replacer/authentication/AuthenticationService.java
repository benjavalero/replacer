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

@Service
@Profile("default")
class AuthenticationService implements IAuthenticationService {

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

    @Override
    public OAuthRequest createOauthRequest() {
        OAuthRequest request = new OAuthRequest(Verb.POST, WIKIPEDIA_API_URL);
        request.addParameter("format", "json");
        request.addParameter("formatversion", "2");
        return request;
    }

    @Override
    public String createOAuthRequest(Map<String, String> params) throws AuthenticationException {
        OAuthRequest request = createOauthRequest();
        params.forEach(request::addParameter);
        try {
            return getOAuthService().execute(request).getBody();
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public Response signAndExecuteOauthRequest(OAuthRequest request, OAuth1AccessToken accessToken)
            throws AuthenticationException {
        try {
            getOAuthService().signRequest(accessToken, request);
            Response response = getOAuthService().execute(request);
            checkOauthResponse(response);
            return response;
        } catch (Exception e) {
            throw new AuthenticationException(e);
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
    public String getEditToken(OAuth1AccessToken accessToken) throws AuthenticationException {
        try {
            OAuthRequest request = createOauthRequest();
            request.addParameter("format", "json");
            request.addParameter("action", "query");
            request.addParameter("meta", "tokens");

            Response response = signAndExecuteOauthRequest(request, accessToken);
            checkOauthResponse(response);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());
            return json.get("query").get("tokens").get("csrftoken").asText();
        } catch (IOException e) {
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
