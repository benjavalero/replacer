package es.bvalero.replacer.user;

import es.bvalero.replacer.auth.AccessToken;
import es.bvalero.replacer.auth.AuthorizationException;
import es.bvalero.replacer.auth.OAuthService;
import es.bvalero.replacer.auth.RequestToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import org.springframework.stereotype.Service;

@Service
class AuthorizationService implements AuthorizationApi {

    // Dependency injection
    private final OAuthService oAuthService;
    private final UserApi userApi;

    AuthorizationService(OAuthService oAuthService, UserApi userApi) {
        this.oAuthService = oAuthService;
        this.userApi = userApi;
    }

    @Override
    public RequestToken getRequestToken() {
        return this.oAuthService.getRequestToken();
    }

    @Override
    public User getAuthenticatedUser(WikipediaLanguage lang, RequestToken requestToken, String oAuthVerifier) {
        AccessToken accessToken = oAuthService.getAccessToken(requestToken, oAuthVerifier);
        return userApi.findAuthenticatedUser(lang, accessToken).orElseThrow(AuthorizationException::new);
    }
}
