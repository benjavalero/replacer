package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("offline")
class AuthenticationServiceMock implements IAuthenticationService {

    @Override
    public OAuthRequest createOauthRequest() {
        return null;
    }

    @Override
    public Response signAndExecuteOauthRequest(OAuthRequest request) {
        return null;
    }

    @Override
    public String getEditToken() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
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
    public OAuth1RequestToken getRequestTokenInSession() {
        return null;
    }

    @Override
    public void setRequestTokenInSession(OAuth1RequestToken requestToken) {
        // Do nothing
    }

    @Override
    public void removeRequestTokenInSession() {
        // Do nothing
    }

    @Override
    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier) {
        return null;
    }

    @Override
    public void setAccessTokenInSession(OAuth1AccessToken accessToken) {
        // Do nothing
    }

    @Override
    public String getRedirectUrlInSession() {
        return null;
    }

    @Override
    public void setRedirectUrlInSession(String url) {
        // Do nothing
    }

    @Override
    public void removeRedirectUrlInSession() {
        // Do nothing
    }
}