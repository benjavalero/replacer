package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;

interface AuthenticationService {
    OAuth1RequestToken getRequestToken() throws AuthenticationException;

    String getAuthorizationUrl(OAuth1RequestToken requestToken);

    OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
        throws AuthenticationException;
}
