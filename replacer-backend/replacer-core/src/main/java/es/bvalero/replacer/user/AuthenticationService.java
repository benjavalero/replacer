package es.bvalero.replacer.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class AuthenticationService {

    @Autowired
    private OAuthService oAuthService;

    RequestToken getRequestToken() throws AuthenticationException {
        return this.oAuthService.getRequestToken();
    }

    String getAuthorizationUrl(RequestToken requestToken) {
        return this.oAuthService.getAuthorizationUrl(requestToken);
    }

    AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) throws AuthenticationException {
        return this.oAuthService.getAccessToken(requestToken, oAuthVerifier);
    }
}
