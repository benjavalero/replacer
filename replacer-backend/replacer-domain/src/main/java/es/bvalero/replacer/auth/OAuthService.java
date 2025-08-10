package es.bvalero.replacer.auth;

import es.bvalero.replacer.common.domain.AccessToken;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Service to perform OAuth authorization operations */
@SecondaryPort
public interface OAuthService {
    /** Get a request token with an authorization URL to initiate an authorization process */
    RequestToken getRequestToken();

    /** Verify the authorization process and get the access token */
    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier);
}
