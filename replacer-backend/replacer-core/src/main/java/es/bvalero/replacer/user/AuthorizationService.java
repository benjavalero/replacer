package es.bvalero.replacer.user;

import org.springframework.stereotype.Service;

@Service
class AuthorizationService {

    // Dependency injection
    private final OAuthService oAuthService;

    AuthorizationService(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    RequestToken getRequestToken() {
        return this.oAuthService.getRequestToken();
    }

    String getAuthorizationUrl(RequestToken requestToken) {
        return this.oAuthService.getAuthorizationUrl(requestToken);
    }

    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) {
        return this.oAuthService.getAccessToken(requestToken, oAuthVerifier);
    }
}
