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
    public OAuth1RequestToken getRequestToken() throws AuthenticationException {
        try {
            return oAuthService.getRequestToken();
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            throw new AuthenticationException();
        }
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return oAuthService.getAuthorizationUrl(requestToken);
    }

    @Override
    public AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
        throws AuthenticationException {
        try {
            return convertToDto(oAuthService.getAccessToken(requestToken, oauthVerifier));
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            throw new AuthenticationException();
        }
    }

    private AccessToken convertToDto(OAuth1AccessToken oAuth1AccessToken) {
        return AccessToken.of(oAuth1AccessToken.getToken(), oAuth1AccessToken.getTokenSecret());
    }
}
