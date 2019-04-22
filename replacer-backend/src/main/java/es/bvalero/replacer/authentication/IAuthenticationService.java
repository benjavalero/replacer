package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface IAuthenticationService {

    OAuthRequest createOauthRequest();

    Response signAndExecuteOauthRequest(OAuthRequest request, OAuth1AccessToken accessToken)
            throws AuthenticationException;

    String getEditToken(OAuth1AccessToken accessToken) throws AuthenticationException;

    String getAuthorizationUrl(OAuth1RequestToken requestToken);

    OAuth1RequestToken getRequestToken() throws InterruptedException, ExecutionException, IOException;

    OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
            throws InterruptedException, ExecutionException, IOException;

}