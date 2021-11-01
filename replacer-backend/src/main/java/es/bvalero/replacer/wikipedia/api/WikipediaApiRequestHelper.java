package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.domain.AccessToken;
import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Helper to perform Wikipedia API requests */
@Slf4j
@Component
class WikipediaApiRequestHelper {

    private static final String WIKIPEDIA_API_URL = "https://%s.wikipedia.org/w/api.php";

    @Autowired
    @Qualifier("mediaWikiApiService")
    private OAuth10aService mediaWikiApiService;

    @Autowired
    private ObjectMapper jsonMapper;

    @Loggable(prepend = true, value = Loggable.TRACE)
    WikipediaApiResponse executeApiRequest(WikipediaApiRequest apiRequest) throws ReplacerException {
        // Add common parameters to receive a JSON response from Wikipedia API
        WikipediaApiRequest request = apiRequest
            .toBuilder()
            .param("format", "json")
            .param("formatversion", "2")
            .build();

        String responseBody = executeMediaWikiRequest(request);
        return convert(responseBody);
    }

    private String executeMediaWikiRequest(WikipediaApiRequest apiRequest) throws ReplacerException {
        Verb verb = convertVerb(apiRequest.getVerb());
        String url = buildWikipediaRequestUrl(apiRequest.getLang());
        OAuthRequest mediaWikiRequest = new OAuthRequest(verb, url);
        apiRequest.getParams().forEach(mediaWikiRequest::addParameter);
        AccessToken accessToken = apiRequest.getAccessToken();
        // Access token can be empty in tests
        if (accessToken != null && !accessToken.isEmpty()) {
            OAuth1AccessToken oAuth1AccessToken = convertAccessToken(accessToken);
            mediaWikiApiService.signRequest(oAuth1AccessToken, mediaWikiRequest);
        }

        try {
            Response response = mediaWikiApiService.execute(mediaWikiRequest);
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

    private Verb convertVerb(WikipediaApiRequestVerb verb) {
        return Verb.valueOf(verb.name());
    }

    private String buildWikipediaRequestUrl(WikipediaLanguage lang) {
        return String.format(WIKIPEDIA_API_URL, lang.getCode());
    }

    private OAuth1AccessToken convertAccessToken(AccessToken accessToken) {
        return new OAuth1AccessToken(accessToken.getToken(), accessToken.getTokenSecret());
    }

    private WikipediaApiResponse convert(String responseBody) throws ReplacerException {
        LOGGER.trace("OAuth response body: {}", responseBody);
        try {
            WikipediaApiResponse apiResponse = jsonMapper.readValue(responseBody, WikipediaApiResponse.class);
            apiResponse.validate();
            return apiResponse;
        } catch (JsonProcessingException e) {
            throw new ReplacerException(e);
        }
    }
}
