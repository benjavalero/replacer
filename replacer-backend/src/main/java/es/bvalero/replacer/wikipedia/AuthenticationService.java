package es.bvalero.replacer.wikipedia;

interface AuthenticationService {
    RequestToken getRequestToken() throws AuthenticationException;

    AccessToken getAccessToken(String requestToken, String requestTokenSecret, String oauthVerifier)
        throws AuthenticationException;
}
