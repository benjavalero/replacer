package es.bvalero.replacer.authentication.oauth;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!offline")
class OAuthMediaWikiService implements OAuthService {

    @Autowired
    @Qualifier("oAuthMediaWikiService")
    private OAuth10aService oAuth10aService;

    @Override
    public RequestToken getRequestToken() throws ReplacerException {
        try {
            return convertRequestToken(oAuth10aService.getRequestToken());
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            throw new ReplacerException(e);
        } catch (ExecutionException | IOException e) {
            throw new ReplacerException(e);
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
    public AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier) throws ReplacerException {
        try {
            return convertAccessToken(
                oAuth10aService.getAccessToken(convertToRequestToken(requestToken), oAuthVerifier)
            );
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            throw new ReplacerException(e);
        } catch (ExecutionException | IOException e) {
            throw new ReplacerException(e);
        }
    }

    private AccessToken convertAccessToken(OAuth1AccessToken oAuth1AccessToken) {
        return AccessToken.of(oAuth1AccessToken.getToken(), oAuth1AccessToken.getTokenSecret());
    }
}
