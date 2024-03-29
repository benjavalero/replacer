package es.bvalero.replacer.user;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("offline")
class OAuthOfflineService implements OAuthService {

    private static final String OFFLINE_REQUEST_TOKEN = "offline-request-token";
    private static final String OFFLINE_REQUEST_TOKEN_SECRET = "offline-request-token-secret";
    private static final String AUTHORIZATION_URL = "/?oauth_verifier=x";
    private static final String OFFLINE_ACCESS_TOKEN = "offline-access-token";
    private static final String OFFLINE_ACCESS_TOKEN_SECRET = "offline-access-token-secret";

    @Override
    public RequestToken getRequestToken() {
        return RequestToken.of(OFFLINE_REQUEST_TOKEN, OFFLINE_REQUEST_TOKEN_SECRET, AUTHORIZATION_URL);
    }

    @Override
    public AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) {
        return AccessToken.of(OFFLINE_ACCESS_TOKEN, OFFLINE_ACCESS_TOKEN_SECRET);
    }
}
