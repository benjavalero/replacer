package es.bvalero.replacer.auth;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.common.domain.AccessToken;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!offline")
class OAuthMediaWikiService implements OAuthService {

    // Dependency injection
    private final OAuth10aService mediaWikiService;

    OAuthMediaWikiService(@Qualifier("mediaWikiService") OAuth10aService mediaWikiService) {
        this.mediaWikiService = mediaWikiService;
    }

    @Override
    public RequestToken getRequestToken() {
        try {
            OAuth1RequestToken oAuth1RequestToken = mediaWikiService.getRequestToken();
            String authorizationUrl = mediaWikiService.getAuthorizationUrl(oAuth1RequestToken);
            return buildRequestToken(oAuth1RequestToken, authorizationUrl);
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            LOGGER.error("Error getting the request token", e);
            throw new AuthorizationException();
        } catch (ExecutionException | IOException e) {
            LOGGER.error("Error getting the request token", e);
            throw new AuthorizationException();
        }
    }

    private RequestToken buildRequestToken(OAuth1RequestToken oAuth1RequestToken, String authorizationUrl) {
        return RequestToken.of(oAuth1RequestToken.getToken(), oAuth1RequestToken.getTokenSecret(), authorizationUrl);
    }

    @Override
    public AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) {
        try {
            return convertAccessToken(
                mediaWikiService.getAccessToken(convertToRequestToken(requestToken), oAuthVerifier)
            );
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            LOGGER.error("Error getting the access token", e);
            throw new AuthorizationException();
        } catch (ExecutionException | IOException e) {
            LOGGER.error("Error getting the access token", e);
            throw new AuthorizationException();
        }
    }

    private OAuth1RequestToken convertToRequestToken(RequestToken authorizationToken) {
        return new OAuth1RequestToken(authorizationToken.getToken(), authorizationToken.getTokenSecret());
    }

    private AccessToken convertAccessToken(OAuth1AccessToken oAuth1AccessToken) {
        return AccessToken.of(oAuth1AccessToken.getToken(), oAuth1AccessToken.getTokenSecret());
    }
}
