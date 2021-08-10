package es.bvalero.replacer.wikipedia.authentication;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service to perform authentication operations */
@Service
public class AuthenticationService {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private WikipediaService wikipediaService;

    @Setter // For testing
    @Value("${replacer.admin.user}")
    private String adminUser;

    RequestToken getRequestToken() throws ReplacerException {
        OAuthToken requestToken = oAuthService.getRequestToken();
        String authorizationUrl = oAuthService.getAuthorizationUrl(requestToken);
        return RequestToken.of(requestToken.getToken(), requestToken.getTokenSecret(), authorizationUrl);
    }

    AuthenticateResponse authenticate(WikipediaLanguage lang, OAuthToken requestToken, String oAuthVerifier)
        throws ReplacerException {
        OAuthToken accessToken = oAuthService.getAccessToken(requestToken, oAuthVerifier);
        WikipediaUser wikipediaUser = wikipediaService.getAuthenticatedUser(lang, accessToken);
        String userName = wikipediaUser.getName();
        boolean hasRights = wikipediaUser.getGroups().contains(WikipediaUserGroup.AUTOCONFIRMED);
        boolean bot = wikipediaUser.getGroups().contains(WikipediaUserGroup.BOT);
        boolean admin = this.isAdminUser(userName);
        return AuthenticateResponse.of(
            userName,
            hasRights,
            bot,
            admin,
            accessToken.getToken(),
            accessToken.getTokenSecret()
        );
    }

    public boolean isAdminUser(String username) {
        return this.adminUser.equals(username);
    }
}
