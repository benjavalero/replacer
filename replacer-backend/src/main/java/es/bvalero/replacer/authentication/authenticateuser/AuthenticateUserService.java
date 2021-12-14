package es.bvalero.replacer.authentication.authenticateuser;

import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.oauth.OAuthService;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import es.bvalero.replacer.authentication.useradmin.CheckUserAdminService;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticateUserService {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private CheckUserAdminService checkUserAdminService;

    public AuthenticatedUser authenticateUser(WikipediaLanguage lang, RequestToken requestToken, String oAuthVerifier)
        throws AuthenticationException {
        try {
            AccessToken accessToken = oAuthService.getAccessToken(requestToken, oAuthVerifier);
            WikipediaUser wikipediaUser = wikipediaService.getAuthenticatedUser(lang, accessToken);
            return toDto(wikipediaUser, accessToken);
        } catch (ReplacerException e) {
            LOGGER.error("Error authenticating the user");
            throw new AuthenticationException();
        }
    }

    private AuthenticatedUser toDto(WikipediaUser wikipediaUser, AccessToken accessToken) {
        return AuthenticatedUser
            .builder()
            .name(wikipediaUser.getName())
            .hasRights(hasRights(wikipediaUser))
            .bot(isBot(wikipediaUser))
            .admin(checkUserAdminService.isAdminUser(wikipediaUser.getName()))
            .token(accessToken.getToken())
            .tokenSecret(accessToken.getTokenSecret())
            .build();
    }

    private boolean hasRights(WikipediaUser wikipediaUser) {
        return wikipediaUser.getGroups().contains(WikipediaUserGroup.AUTOCONFIRMED);
    }

    private boolean isBot(WikipediaUser wikipediaUser) {
        return wikipediaUser.getGroups().contains(WikipediaUserGroup.BOT);
    }
}
