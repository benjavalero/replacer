package es.bvalero.replacer.wikipedia.authentication;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthService;
import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.UserInfo;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service to perform authentication operations */
@Service
public class AuthenticationService {

    static final String GROUP_AUTOCONFIRMED = "autoconfirmed";
    private static final String GROUP_BOT = "bot";

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
        UserInfo userInfo = wikipediaService.getUserInfo(lang, accessToken);
        String userName = userInfo.getName();
        boolean hasRights = userInfo.getGroups().contains(GROUP_AUTOCONFIRMED);
        boolean bot = userInfo.getGroups().contains(GROUP_BOT);
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
