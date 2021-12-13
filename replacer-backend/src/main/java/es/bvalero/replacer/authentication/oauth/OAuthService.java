package es.bvalero.replacer.authentication.oauth;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.exception.ReplacerException;

/** Service to perform OAuth authentication operations */
public interface OAuthService {
    RequestToken getRequestToken() throws ReplacerException;

    String getAuthorizationUrl(RequestToken requestToken);

    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) throws ReplacerException;
}
