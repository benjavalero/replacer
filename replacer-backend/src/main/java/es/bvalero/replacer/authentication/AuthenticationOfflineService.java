package es.bvalero.replacer.authentication;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
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
    public AuthenticateResponse authenticate(WikipediaLanguage lang, OAuthToken requestToken, String oAuthVerifier)
        throws ReplacerException {
        OAuthToken accessToken = OAuthToken.ofEmpty();
        WikipediaUser wikipediaUser = wikipediaService.getAuthenticatedUser(lang, accessToken);
        return AuthenticateResponse.of(
            wikipediaUser.getName(),
            wikipediaUser.hasRights(),
            wikipediaUser.isBot(),
            wikipediaUser.isAdmin(),
            accessToken.getToken(),
            accessToken.getTokenSecret()
        );
    }
}
