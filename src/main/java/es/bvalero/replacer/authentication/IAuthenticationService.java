package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface IAuthenticationService {

    OAuthRequest createOauthRequest();

    Response signAndExecuteOauthRequest(OAuthRequest request) throws AuthenticationException;

    String getEditToken() throws AuthenticationException;

    boolean isAuthenticated();

    String getAuthorizationUrl(OAuth1RequestToken requestToken);

    /* REQUEST TOKEN */

    OAuth1RequestToken getRequestToken() throws InterruptedException, ExecutionException, IOException;

    OAuth1RequestToken getRequestTokenInSession();

    void setRequestTokenInSession(OAuth1RequestToken requestToken);

    void removeRequestTokenInSession();

    /* ACCESS TOKEN */

    OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
            throws InterruptedException, ExecutionException, IOException;

    void setAccessTokenInSession(OAuth1AccessToken accessToken);

    /* REDIRECT TOKEN */

    String getRedirectUrlInSession();

    void setRedirectUrlInSession(String url);

    void removeRedirectUrlInSession();

}