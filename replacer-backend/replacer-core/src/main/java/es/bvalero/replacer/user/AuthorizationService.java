package es.bvalero.replacer.user;

import org.springframework.stereotype.Service;

/**
 * Service to perform OAuth authorization operations.
 * It is just a proxy so the web adapter cannot call directly to the OAuth adapter.
 */
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
