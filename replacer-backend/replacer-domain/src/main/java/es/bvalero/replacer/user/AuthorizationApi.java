package es.bvalero.replacer.user;

/** Service to perform authorization operations */
interface AuthorizationApi {
    /** Get a request token with an authorization URL to initiate an authorization process */
    RequestToken getRequestToken();

    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier);
}
