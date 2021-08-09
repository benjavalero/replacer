package es.bvalero.replacer.wikipedia.oauth;

import es.bvalero.replacer.wikipedia.OAuthService;
import es.bvalero.replacer.wikipedia.OAuthToken;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("offline")
class OAuthOfflineService implements OAuthService {

    private static final String AUTHORIZATION_URL = "/?oauth_verifier=x";

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
