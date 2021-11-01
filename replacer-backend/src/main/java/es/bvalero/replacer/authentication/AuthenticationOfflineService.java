package es.bvalero.replacer.authentication;

import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("offline")
class AuthenticationOfflineService implements AuthenticationService {

    private static final String AUTHORIZATION_URL = "/?oauth_verifier=x";

    @Autowired
    private WikipediaService wikipediaService;

    @Override
    public OAuthToken getRequestToken() {
        return OAuthToken.ofEmpty();
    }

    @Override
    public String getAuthorizationUrl(OAuthToken requestToken) {
        return AUTHORIZATION_URL;
    }

    @Override
    public OAuthToken getAccessToken(OAuthToken requestToken, String oAuthVerifier) {
        return OAuthToken.ofEmpty();
    }
}
