package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Facade for Wikipedia API for common operations: authentication and requests.
 */
@Slf4j
@Service
class WikipediaApiFacade {

    private static final String WIKIPEDIA_API_URL = "https://%s.wikipedia.org/w/api.php";

    @Autowired
    private OAuth10aService oAuthService;

    @Autowired
    private ObjectMapper jsonMapper;

    RequestToken getRequestToken() throws ReplacerException {
        try {
            OAuth1RequestToken oAuth1RequestToken = oAuthService.getRequestToken();
            String authorizationUrl = oAuthService.getAuthorizationUrl(oAuth1RequestToken);
            return convert(oAuth1RequestToken, authorizationUrl);
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            throw new ReplacerException(e);
        } catch (ExecutionException | IOException e) {
            throw new ReplacerException(e);
        }
    }

    private RequestToken convert(OAuth1RequestToken oAuth1RequestToken, String authorizationUrl) {
        return RequestToken.of(oAuth1RequestToken.getToken(), oAuth1RequestToken.getTokenSecret(), authorizationUrl);
    }

    AccessToken getAccessToken(String requestToken, String requestTokenSecret, String oauthVerifier)
        throws ReplacerException {
        try {
            OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken(requestToken, requestTokenSecret);
            OAuth1AccessToken oAuth1AccessToken = oAuthService.getAccessToken(oAuth1RequestToken, oauthVerifier);
            return convert(oAuth1AccessToken);
        } catch (InterruptedException e) {
            // This cannot be unit-tested because the mocked InterruptedException make other tests fail
            Thread.currentThread().interrupt();
            throw new ReplacerException(e);
        } catch (ExecutionException | IOException e) {
            throw new ReplacerException(e);
        }
    }

    private AccessToken convert(OAuth1AccessToken oAuth1AccessToken) {
        return AccessToken.of(oAuth1AccessToken.getToken(), oAuth1AccessToken.getTokenSecret());
    }

    WikipediaApiResponse executeGetRequest(Map<String, String> params, WikipediaLanguage lang)
        throws ReplacerException {
        return executeRequest(params, Verb.GET, lang, null);
    }

    WikipediaApiResponse executeSignedGetRequest(
        Map<String, String> params,
        WikipediaLanguage lang,
        AccessToken accessToken
    ) throws ReplacerException {
        return executeRequest(params, Verb.GET, lang, accessToken);
    }

    WikipediaApiResponse executeSignedPostRequest(
        Map<String, String> params,
        WikipediaLanguage lang,
        AccessToken accessToken
    ) throws ReplacerException {
        return executeRequest(params, Verb.POST, lang, accessToken);
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    private WikipediaApiResponse executeRequest(
        Map<String, String> params,
        Verb verb,
        WikipediaLanguage lang,
        @Nullable AccessToken accessToken
    ) throws ReplacerException {
        // Access token may be an empty string in tests
        boolean isSigned = accessToken != null && StringUtils.isNotBlank(accessToken.getToken());
        LOGGER.trace("OAuth request is signed: {}", isSigned);
        OAuthRequest request = createOAuthRequestWithParams(params, verb, lang);

        if (isSigned) {
            signOAuthRequest(request, convert(accessToken));
        }

        try {
            Response response = oAuthService.execute(request);
            if (!response.isSuccessful()) {
                throw new ReplacerException(
                    String.format("Call not successful: %d - %s", response.getCode(), response.getMessage())
                );
            }

            return convert(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReplacerException("ERROR executing OAuth Request", e);
        } catch (ExecutionException | IOException | IllegalArgumentException | NullPointerException e) {
            throw new ReplacerException("ERROR executing OAuth Request", e);
        }
    }

    private OAuthRequest createOAuthRequestWithParams(Map<String, String> params, Verb verb, WikipediaLanguage lang) {
        String url = String.format(WIKIPEDIA_API_URL, lang.getCode());
        OAuthRequest request = new OAuthRequest(verb, url);

        // Add request parameter
        params.forEach(request::addParameter);

        // Add common parameters to receive a JSON response from Wikipedia API
        request.addParameter("format", "json");
        request.addParameter("formatversion", "2");

        return request;
    }

    private OAuth1AccessToken convert(AccessToken accessToken) {
        return new OAuth1AccessToken(accessToken.getToken(), accessToken.getTokenSecret());
    }

    private void signOAuthRequest(OAuthRequest request, OAuth1AccessToken accessToken) {
        oAuthService.signRequest(accessToken, request);
    }

    private WikipediaApiResponse convert(Response response) throws IOException, ReplacerException {
        LOGGER.trace("OAuth response body: {}", response.getBody());
        WikipediaApiResponse apiResponse = jsonMapper.readValue(response.getBody(), WikipediaApiResponse.class);
        apiResponse.validate();
        return apiResponse;
    }
}
