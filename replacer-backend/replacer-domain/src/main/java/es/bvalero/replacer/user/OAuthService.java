package es.bvalero.replacer.user;

import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Service to perform OAuth authentication operations */
@SecondaryPort
interface OAuthService {
    RequestToken getRequestToken();

    String getAuthorizationUrl(RequestToken requestToken);

    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier);
}
