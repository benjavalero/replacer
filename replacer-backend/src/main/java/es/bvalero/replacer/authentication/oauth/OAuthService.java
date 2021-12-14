package es.bvalero.replacer.authentication.oauth;

import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.common.domain.AccessToken;

/** Service to perform OAuth authentication operations */
public interface OAuthService {
    RequestToken getRequestToken() throws AuthenticationException;

    String getAuthorizationUrl(RequestToken requestToken);

    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) throws AuthenticationException;
}
