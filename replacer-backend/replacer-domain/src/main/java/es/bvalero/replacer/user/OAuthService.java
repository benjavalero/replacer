package es.bvalero.replacer.user;

import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Service to perform OAuth authorization operations */
@SecondaryPort
interface OAuthService {
    RequestToken getRequestToken();

    String getAuthorizationUrl(RequestToken requestToken);

    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier);
}
