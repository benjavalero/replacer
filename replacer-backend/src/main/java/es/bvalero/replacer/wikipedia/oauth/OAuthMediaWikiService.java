package es.bvalero.replacer.wikipedia.oauth;

import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.wikipedia.OAuthService;
import es.bvalero.replacer.wikipedia.OAuthToken;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * OAuth1 implementation using Scribe-Java library
 */
@Slf4j
@Service
@Profile("!offline")
class OAuthMediaWikiService implements OAuthService {

    @Autowired
    private OAuth10aService oAuth10aService;

    @Override
    public OAuthToken getRequestToken() throws ReplacerException {
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

    private OAuthToken convertRequestToken(OAuth1RequestToken oAuth1RequestToken) {
        return OAuthToken.of(oAuth1RequestToken.getToken(), oAuth1RequestToken.getTokenSecret());
    }

    @Override
    public String getAuthorizationUrl(OAuthToken requestToken) {
        return oAuth10aService.getAuthorizationUrl(convertToRequestToken(requestToken));
    }

    private OAuth1RequestToken convertToRequestToken(OAuthToken authenticationToken) {
        return new OAuth1RequestToken(authenticationToken.getToken(), authenticationToken.getTokenSecret());
    }

    @Override
    public OAuthToken getAccessToken(OAuthToken requestToken, String oAuthVerifier) throws ReplacerException {
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

    private OAuthToken convertAccessToken(OAuth1AccessToken oAuth1AccessToken) {
        return OAuthToken.of(oAuth1AccessToken.getToken(), oAuth1AccessToken.getTokenSecret());
    }

    @Override
    public String executeRequest(String verb, String url, Map<String, String> parameters) throws ReplacerException {
        return executeMediaWikiRequest(Verb.valueOf(verb), url, parameters, null);
    }

    @Override
    public String executeSignedRequest(String verb, String url, Map<String, String> parameters, OAuthToken oAuthToken)
        throws ReplacerException {
        // Access token may be an empty string in tests
        if (StringUtils.isNotBlank(oAuthToken.getToken())) {
            return executeMediaWikiRequest(Verb.valueOf(verb), url, parameters, convertToAccessToken(oAuthToken));
        } else {
            return executeMediaWikiRequest(Verb.valueOf(verb), url, parameters, null);
        }
    }

    private OAuth1AccessToken convertToAccessToken(OAuthToken oAuthToken) {
        return new OAuth1AccessToken(oAuthToken.getToken(), oAuthToken.getTokenSecret());
    }

    private String executeMediaWikiRequest(
        Verb verb,
        String url,
        Map<String, String> parameters,
        @Nullable OAuth1AccessToken accessToken
    ) throws ReplacerException {
        OAuthRequest request = new OAuthRequest(verb, url);
        parameters.forEach(request::addParameter);
        if (accessToken != null) {
            oAuth10aService.signRequest(accessToken, request);
        }

        try {
            Response response = oAuth10aService.execute(request);
            if (!response.isSuccessful()) {
                throw new ReplacerException(
                    String.format("Call not successful: %d - %s", response.getCode(), response.getMessage())
                );
            }

            return response.getBody();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReplacerException("ERROR executing OAuth Request", e);
        } catch (ExecutionException | IOException | IllegalArgumentException | NullPointerException e) {
            throw new ReplacerException("ERROR executing OAuth Request", e);
        }
    }
}
