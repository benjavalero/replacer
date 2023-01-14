package es.bvalero.replacer.user;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!offline")
class OAuthMediaWikiService implements OAuthService {

    @Autowired
    @Qualifier("oAuthMediaWikiService")
    private OAuth10aService oAuth10aService;

    @Override
    public RequestToken getRequestToken() throws AuthenticationException {
        try {
            return convertRequestToken(oAuth10aService.getRequestToken());
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            LOGGER.error("Error getting the request token", e);
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            LOGGER.error("Error getting the request token", e);
            throw new AuthenticationException();
        }
    }

    private RequestToken convertRequestToken(OAuth1RequestToken oAuth1RequestToken) {
        return RequestToken.of(oAuth1RequestToken.getToken(), oAuth1RequestToken.getTokenSecret());
    }

    @Override
    public String getAuthorizationUrl(RequestToken requestToken) {
        return oAuth10aService.getAuthorizationUrl(convertToRequestToken(requestToken));
    }

    private OAuth1RequestToken convertToRequestToken(RequestToken authenticationToken) {
        return new OAuth1RequestToken(authenticationToken.getToken(), authenticationToken.getTokenSecret());
    }

    @Override
    public AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) throws AuthenticationException {
        try {
            return convertAccessToken(
                oAuth10aService.getAccessToken(convertToRequestToken(requestToken), oAuthVerifier)
            );
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            LOGGER.error("Error getting the access token", e);
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            LOGGER.error("Error getting the access token", e);
            throw new AuthenticationException();
        }
    }

    private AccessToken convertAccessToken(OAuth1AccessToken oAuth1AccessToken) {
        return AccessToken.of(oAuth1AccessToken.getToken(), oAuth1AccessToken.getTokenSecret());
    }
}
