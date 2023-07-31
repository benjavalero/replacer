package es.bvalero.replacer.user;

/** Service to perform OAuth authentication operations */
interface OAuthService {
    RequestToken getRequestToken();

    String getAuthorizationUrl(RequestToken requestToken);

    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier);
}
