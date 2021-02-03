package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1RequestToken;

interface AuthenticationService {
    OAuth1RequestToken getRequestToken() throws AuthenticationException;

    String getAuthorizationUrl(OAuth1RequestToken requestToken);

    AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier) throws AuthenticationException;
}
