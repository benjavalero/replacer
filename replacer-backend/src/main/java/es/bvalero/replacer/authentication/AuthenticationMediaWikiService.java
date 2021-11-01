package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.wikipedia.OAuthToken;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!offline")
class AuthenticationMediaWikiService implements AuthenticationService {

    @Autowired
    @Qualifier("oAuthMediaWikiService")
    private OAuth10aService oAuthMediaWikiService;

    @Override
    public OAuthToken getRequestToken() throws ReplacerException {
        try {
            return convertRequestToken(oAuthMediaWikiService.getRequestToken());
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            throw new ReplacerException(e);
        } catch (ExecutionException | IOException e) {
            throw new ReplacerException(e);
        }
    }

    private OAuthToken convertRequestToken(OAuth1RequestToken oAuth1RequestToken) {
        return OAuthToken.of(oAuth1RequestToken.getToken(), oAuth1RequestToken.getTokenSecret());
    }

    @Override
    public String getAuthorizationUrl(OAuthToken requestToken) {
        return oAuthMediaWikiService.getAuthorizationUrl(convertToRequestToken(requestToken));
    }

    @VisibleForTesting
    OAuth1RequestToken convertToRequestToken(OAuthToken authenticationToken) {
        return new OAuth1RequestToken(authenticationToken.getToken(), authenticationToken.getTokenSecret());
    }

    @Override
    public OAuthToken getAccessToken(OAuthToken requestToken, String oAuthVerifier) throws ReplacerException {
        try {
            return convertAccessToken(
                oAuthMediaWikiService.getAccessToken(convertToRequestToken(requestToken), oAuthVerifier)
            );
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            throw new ReplacerException(e);
        } catch (ExecutionException | IOException e) {
            throw new ReplacerException(e);
        }
    }

    private OAuthToken convertAccessToken(OAuth1AccessToken oAuth1AccessToken) {
        return OAuthToken.of(oAuth1AccessToken.getToken(), oAuth1AccessToken.getTokenSecret());
    }
}
