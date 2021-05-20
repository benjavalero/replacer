package es.bvalero.replacer.wikipedia.authentication;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthService;
import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.UserInfo;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.Setter;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service to perform authentication operations */
@Service
class AuthenticationService {

    static final String GROUP_AUTOCONFIRMED = "autoconfirmed";

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

    WikipediaUser getLoggedUser(WikipediaLanguage lang, OAuthToken requestToken, String oAuthVerifier)
        throws ReplacerException {
        OAuthToken accessToken = oAuthService.getAccessToken(requestToken, oAuthVerifier);
        UserInfo userInfo = wikipediaService.getUserInfo(lang, accessToken);
        String userName = userInfo.getName();
        boolean hasRights = userInfo.getGroups().contains(GROUP_AUTOCONFIRMED);
        boolean admin = this.isAdminUser(userName);
        return WikipediaUser.of(userName, hasRights, admin, accessToken.getToken(), accessToken.getTokenSecret());
    }

    @VisibleForTesting
    boolean isAdminUser(String username) {
        return this.adminUser.equals(username);
    }
}
