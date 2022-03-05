package es.bvalero.replacer.authentication.authenticateuser;

import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.oauth.OAuthService;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import es.bvalero.replacer.authentication.userrights.CheckUserRightsService;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.AccessTokenDto;
import es.bvalero.replacer.common.domain.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticateUserService {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private WikipediaUserRepository wikipediaUserRepository;

    @Autowired
    private CheckUserRightsService checkUserRightsService;

    public AuthenticatedUser authenticateUser(WikipediaLanguage lang, RequestToken requestToken, String oAuthVerifier)
        throws AuthenticationException {
            AccessToken accessToken = oAuthService.getAccessToken(requestToken, oAuthVerifier);
            WikipediaUser wikipediaUser = wikipediaUserRepository.findAuthenticatedUser(lang, accessToken)
                .orElseThrow(AuthenticationException::new);
            return toDto(wikipediaUser, accessToken);
    }

    private AuthenticatedUser toDto(WikipediaUser wikipediaUser, AccessToken accessToken) {
        return AuthenticatedUser
            .builder()
            .name(wikipediaUser.getName())
            .hasRights(wikipediaUser.hasRights())
            .bot(wikipediaUser.isBot())
            .admin(checkUserRightsService.isAdmin(wikipediaUser.getName()))
            .accessToken(AccessTokenDto.fromDomain(accessToken))
            .build();
    }
}
