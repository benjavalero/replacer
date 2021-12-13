package es.bvalero.replacer.authentication.requesttoken;

import es.bvalero.replacer.authentication.oauth.OAuthService;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import es.bvalero.replacer.common.exception.ReplacerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetRequestTokenService {

    @Autowired
    private OAuthService oAuthService;

    public GetRequestTokenResponse get() throws ReplacerException {
        RequestToken requestToken = oAuthService.getRequestToken();
        String authorizationUrl = oAuthService.getAuthorizationUrl(requestToken);
        return GetRequestTokenResponse.of(requestToken.getToken(), requestToken.getTokenSecret(), authorizationUrl);
    }
}
