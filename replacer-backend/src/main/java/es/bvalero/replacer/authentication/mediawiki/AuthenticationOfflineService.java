package es.bvalero.replacer.authentication.mediawiki;

import es.bvalero.replacer.authentication.AuthenticationService;
import es.bvalero.replacer.authentication.RequestToken;
import es.bvalero.replacer.common.domain.AccessToken;
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
    private static final String OFFLINE_ACCESS_TOKEN = "offline-access-token";
    private static final String OFFLINE_ACCESS_TOKEN_SECRET = "offline-access-token-secret";

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
    public AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) {
        return AccessToken.of(OFFLINE_ACCESS_TOKEN, OFFLINE_ACCESS_TOKEN_SECRET);
    }
}
