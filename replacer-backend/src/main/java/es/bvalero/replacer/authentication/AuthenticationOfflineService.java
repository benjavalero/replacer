package es.bvalero.replacer.authentication;

import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("offline")
class AuthenticationOfflineService implements AuthenticationService {

    private static final String OFFLINE_REQUEST_TOKEN = "offline-request-token";
    private static final String OFFLINE_REQUEST_TOKEN_SECRET = "offline-request-token-secret";
    private static final String AUTHORIZATION_URL = "/?oauth_verifier=x";

    @Autowired
    private WikipediaService wikipediaService;

    @Override
    public RequestToken getRequestToken() {
        return RequestToken.of(OFFLINE_REQUEST_TOKEN, OFFLINE_REQUEST_TOKEN_SECRET);
    }

    @Override
    public String getAuthorizationUrl(RequestToken requestToken) {
        return AUTHORIZATION_URL;
    }

    @Override
    public OAuthToken getAccessToken(RequestToken requestToken, String oAuthVerifier) {
        return OAuthToken.empty();
    }
}
