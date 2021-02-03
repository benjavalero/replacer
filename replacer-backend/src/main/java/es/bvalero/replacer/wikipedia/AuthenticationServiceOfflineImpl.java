package es.bvalero.replacer.wikipedia;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("offline")
class AuthenticationServiceOfflineImpl implements AuthenticationService {

    private static final String AUTHORIZATION_URL = "/?oauth_verifier=x";

    @Override
    public RequestToken getRequestToken() {
        return RequestToken.of("", "", AUTHORIZATION_URL);
    }

    @Override
    public AccessToken getAccessToken(String requestToken, String requestTokenSecret, String oauthVerifier) {
        return AccessToken.ofEmpty();
    }
}
