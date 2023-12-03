package es.bvalero.replacer.user;

import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Service to perform OAuth authorization operations */
@SecondaryPort
interface OAuthService {
    /** Get a request token with an authorization URL to initiate an authorization process */
    RequestToken getRequestToken();

    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier);
}
