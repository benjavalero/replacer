package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;

/** Service to perform authorization operations */
interface AuthorizationApi {
    /** Get a request token with an authorization URL to initiate an authorization process */
    RequestToken getRequestToken();

    /** Verify the authorization process and get the authenticated user */
    User getAuthenticatedUser(WikipediaLanguage lang, RequestToken requestToken, String oAuthVerifier);
}
