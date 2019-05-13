package es.bvalero.replacer.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface AuthenticationService {

    JsonNode executeOAuthRequest(Map<String, String> params, OAuth1AccessToken accessToken) throws AuthenticationException;

    JsonNode executeUnsignedOAuthRequest(Map<String, String> params) throws AuthenticationException;

    String getAuthorizationUrl(OAuth1RequestToken requestToken);

    OAuth1RequestToken getRequestToken() throws InterruptedException, ExecutionException, IOException;

    OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
            throws InterruptedException, ExecutionException, IOException;

}