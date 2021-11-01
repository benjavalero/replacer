package es.bvalero.replacer.authentication;

import es.bvalero.replacer.domain.AccessToken;
import es.bvalero.replacer.domain.ReplacerException;

/** Service to perform authentication operations */
public interface AuthenticationService {
    RequestToken getRequestToken() throws ReplacerException;

    String getAuthorizationUrl(RequestToken requestToken);

    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) throws ReplacerException;
}
