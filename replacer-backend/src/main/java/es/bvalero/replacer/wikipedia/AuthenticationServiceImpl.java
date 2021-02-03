package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!offline")
class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private OAuth10aService oAuthService;

    @Override
    public RequestToken getRequestToken() throws AuthenticationException {
        try {
            OAuth1RequestToken requestToken = oAuthService.getRequestToken();
            String authorizationUrl = this.getAuthorizationUrl(requestToken);
            return convertToDto(requestToken, authorizationUrl);
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            throw new AuthenticationException();
        }
    }

    private String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return oAuthService.getAuthorizationUrl(requestToken);
    }

    @Override
    public AccessToken getAccessToken(String requestToken, String requestTokenSecret, String oauthVerifier)
        throws AuthenticationException {
        try {
            OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken(requestToken, requestTokenSecret);
            return convertToDto(oAuthService.getAccessToken(oAuth1RequestToken, oauthVerifier));
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            throw new AuthenticationException();
        }
    }

    private RequestToken convertToDto(OAuth1RequestToken oAuth1RequestToken, String authorizationUrl) {
        return RequestToken.of(oAuth1RequestToken.getToken(), oAuth1RequestToken.getTokenSecret(), authorizationUrl);
    }

    private AccessToken convertToDto(OAuth1AccessToken oAuth1AccessToken) {
        return AccessToken.of(oAuth1AccessToken.getToken(), oAuth1AccessToken.getTokenSecret());
    }
}
