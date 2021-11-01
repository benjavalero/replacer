package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.AccessLevel;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!offline")
class AuthenticationMediaWikiService implements AuthenticationService {

    @Autowired
    @Qualifier("oAuthMediaWikiService")
    private OAuth10aService oAuthMediaWikiService;

    @Autowired
    private WikipediaService wikipediaService;

    @Setter(AccessLevel.PACKAGE) // For testing
    @Value("${replacer.admin.user}")
    private String adminUser;

    @Override
    public RequestToken getRequestToken() throws ReplacerException {
        OAuthToken requestToken = getMediaWikiRequestToken();
        String authorizationUrl = getAuthorizationUrl(requestToken);
        return RequestToken.of(requestToken.getToken(), requestToken.getTokenSecret(), authorizationUrl);
    }

    private OAuthToken getMediaWikiRequestToken() throws ReplacerException {
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

    private String getAuthorizationUrl(OAuthToken requestToken) {
        return oAuthMediaWikiService.getAuthorizationUrl(convertToRequestToken(requestToken));
    }

    @VisibleForTesting
    OAuth1RequestToken convertToRequestToken(OAuthToken authenticationToken) {
        return new OAuth1RequestToken(authenticationToken.getToken(), authenticationToken.getTokenSecret());
    }

    @Override
    public AuthenticateResponse authenticate(WikipediaLanguage lang, OAuthToken requestToken, String oAuthVerifier)
        throws ReplacerException {
        OAuthToken accessToken = getAccessToken(requestToken, oAuthVerifier);
        WikipediaUser wikipediaUser = wikipediaService.getAuthenticatedUser(lang, accessToken);
        return AuthenticateResponse.of(
            wikipediaUser.getName(),
            wikipediaUser.hasRights(),
            wikipediaUser.isBot(),
            wikipediaUser.isAdmin(),
            accessToken.getToken(),
            accessToken.getTokenSecret()
        );
    }

    private OAuthToken getAccessToken(OAuthToken requestToken, String oAuthVerifier) throws ReplacerException {
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

    @TestOnly
    OAuth1AccessToken convertToAccessToken(OAuthToken authenticationToken) {
        return new OAuth1AccessToken(authenticationToken.getToken(), authenticationToken.getTokenSecret());
    }
}
