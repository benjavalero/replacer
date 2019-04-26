package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("offline")
class AuthenticationServiceMock implements IAuthenticationService {

    @Override
    public String executeOAuthRequest(Map<String, String> params) {
        return null;
    }

    @Override
    public Response signAndExecuteOauthRequest(OAuthRequest request, OAuth1AccessToken accessToken) {
        return null;
    }

    @Override
    public String getEditToken(OAuth1AccessToken accessToken) {
        return null;
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return null;
    }

    @Override
    public OAuth1RequestToken getRequestToken() {
        return null;
    }

    @Override
    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier) {
        return null;
    }

}