package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

/** Service to perform authorization operations */
@PrimaryPort
interface AuthorizationApi {
    /** Get a request token with an authorization URL to initiate an authorization process */
    RequestToken getRequestToken();

    /** Verify the authorization process and get the authenticated user */
    User getAuthenticatedUser(WikipediaLanguage lang, RequestToken requestToken, String oAuthVerifier);
}
