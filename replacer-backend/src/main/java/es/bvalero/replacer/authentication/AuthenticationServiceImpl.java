package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!offline")
class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private OAuth10aService oAuthService;

    @Override
    public OAuth1RequestToken getRequestToken() throws AuthenticationException {
        try {
            LOGGER.info("START Get Request Token from MediaWiki API");
            OAuth1RequestToken token = oAuthService.getRequestToken();
            LOGGER.info("END Get Request Token from MediaWiki API. Token: {}", token.getToken());
            return token;
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            LOGGER.error("ERROR getting Request Token from MediaWiki API", e);
            Thread.currentThread().interrupt();
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            LOGGER.error("ERROR getting Request Token from MediaWiki API", e);
            throw new AuthenticationException();
        }
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        LOGGER.info("START Get Authorization URL from MediaWiki API. Request Token: {}", requestToken.getToken());
        String authorizationUrl = oAuthService.getAuthorizationUrl(requestToken);
        LOGGER.info("END Get Authorization URL from MediaWiki API: {}", authorizationUrl);
        return authorizationUrl;
    }

    @Override
    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
        throws AuthenticationException {
        try {
            LOGGER.info("START Get Access Token from MediaWiki API. Request Token: {}", requestToken.getToken());
            OAuth1AccessToken token = oAuthService.getAccessToken(requestToken, oauthVerifier);
            LOGGER.info("END Get Access Token from MediaWiki API: {} / {}", token.getToken(), token.getTokenSecret());
            return token;
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            LOGGER.error("ERROR getting Access Token from MediaWiki API", e);
            Thread.currentThread().interrupt();
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            LOGGER.error("ERROR getting Access Token from MediaWiki API", e);
            throw new AuthenticationException();
        }
    }
}
