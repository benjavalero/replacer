package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
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
    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier) {
        return new OAuth1AccessToken("", "", "");
    }

}