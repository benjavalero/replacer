package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1RequestToken;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("offline")
class AuthenticationServiceOfflineImpl implements AuthenticationService {

    @Override
    public OAuth1RequestToken getRequestToken() {
        return new OAuth1RequestToken("", "", "");
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return "/?oauth_verifier=x";
    }

    @Override
    public AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier) {
        return AccessToken.ofEmpty();
    }
}
