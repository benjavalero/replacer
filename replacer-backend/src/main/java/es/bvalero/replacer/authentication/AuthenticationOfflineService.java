package es.bvalero.replacer.authentication;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
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
    public RequestToken getRequestToken() {
        OAuthToken requestToken = OAuthToken.ofEmpty();
        return RequestToken.of(requestToken.getToken(), requestToken.getTokenSecret(), AUTHORIZATION_URL);
    }

    @Override
    public AuthenticateResponse authenticate(WikipediaLanguage lang, OAuthToken requestToken, String oAuthVerifier)
        throws ReplacerException {
        OAuthToken accessToken = OAuthToken.ofEmpty();
        WikipediaUser wikipediaUser = wikipediaService.getAuthenticatedUser(lang, accessToken);
        String userName = wikipediaUser.getName();
        boolean hasRights = wikipediaUser.getGroups().contains(WikipediaUserGroup.AUTOCONFIRMED);
        boolean bot = wikipediaUser.getGroups().contains(WikipediaUserGroup.BOT);
        return AuthenticateResponse.of(
            userName,
            hasRights,
            bot,
            wikipediaUser.isAdmin(),
            accessToken.getToken(),
            accessToken.getTokenSecret()
        );
    }
}
