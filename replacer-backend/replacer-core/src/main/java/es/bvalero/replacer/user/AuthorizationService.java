package es.bvalero.replacer.user;

import org.springframework.stereotype.Service;

/** Service to perform authorization operations */
@Service
class AuthorizationService implements AuthorizationApi {

    // Dependency injection
    private final OAuthService oAuthService;

    AuthorizationService(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @Override
    public RequestToken getRequestToken() {
        return this.oAuthService.getRequestToken();
    }

    @Override
    public AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) {
        return this.oAuthService.getAccessToken(requestToken, oAuthVerifier);
    }
}
