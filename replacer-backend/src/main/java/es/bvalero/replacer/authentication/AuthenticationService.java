package es.bvalero.replacer.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;

import java.util.Map;

public interface AuthenticationService {

    JsonNode executeOAuthRequest(Map<String, String> params, OAuth1AccessToken accessToken)
            throws AuthenticationException;

    JsonNode executeUnsignedOAuthRequest(Map<String, String> params) throws AuthenticationException;

    String getAuthorizationUrl(OAuth1RequestToken requestToken) throws AuthenticationException;

    OAuth1RequestToken getRequestToken() throws AuthenticationException;

    OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
            throws AuthenticationException;

    String identify(OAuth1AccessToken accessToken) throws AuthenticationException;
}