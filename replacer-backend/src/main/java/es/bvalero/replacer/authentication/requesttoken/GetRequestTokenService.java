package es.bvalero.replacer.authentication.requesttoken;

import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.oauth.OAuthService;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetRequestTokenService {

    @Autowired
    private OAuthService oAuthService;

    public GetRequestTokenResponse get() throws AuthenticationException {
        RequestToken requestToken = oAuthService.getRequestToken();
        String authorizationUrl = oAuthService.getAuthorizationUrl(requestToken);
        return GetRequestTokenResponse.of(requestToken.getToken(), requestToken.getTokenSecret(), authorizationUrl);
    }
}
