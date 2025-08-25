package es.bvalero.replacer.wikipedia.api;

import static org.springframework.http.HttpHeaders.ACCEPT_ENCODING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Helper to perform Wikipedia API requests */
@Slf4j
@Component
public class WikipediaApiHelper {

    // Dependency injection
    private final OAuth10aService mediaWikiService;
    private final ObjectMapper jsonMapper;

    public WikipediaApiHelper(
        @Qualifier("mediaWikiService") OAuth10aService mediaWikiService,
        ObjectMapper jsonMapper
    ) {
        this.mediaWikiService = mediaWikiService;
        this.jsonMapper = jsonMapper;
    }

    public WikipediaApiResponse executeApiRequest(WikipediaApiRequest apiRequest) throws WikipediaException {
        return convert(executeApiRequestAsText(apiRequest));
    }

    public String executeApiRequestAsText(WikipediaApiRequest apiRequest) throws WikipediaException {
        // Add common parameters to receive a JSON response from Wikipedia API
        WikipediaApiRequest request = apiRequest
            .toBuilder()
            .param("format", "json")
            .param("formatversion", "2")
            .build();

        return executeMediaWikiRequest(request);
    }

    private String executeMediaWikiRequest(WikipediaApiRequest apiRequest) throws WikipediaException {
        Verb verb = convertVerb(apiRequest.getVerb());
        String url = apiRequest.getUrl();
        OAuthRequest mediaWikiRequest = new OAuthRequest(verb, url);
        mediaWikiRequest.addHeader(ACCEPT_ENCODING, "gzip");
        apiRequest.getParams().forEach(mediaWikiRequest::addParameter);
        if (apiRequest.isSigned()) {
            assert apiRequest.getAccessToken() != null;
            OAuth1AccessToken oAuth1AccessToken = convertAccessToken(apiRequest.getAccessToken());
            mediaWikiService.signRequest(oAuth1AccessToken, mediaWikiRequest);
        }

        try {
            Response response = mediaWikiService.execute(mediaWikiRequest);
            if (!response.isSuccessful()) {
                throw new WikipediaException(
                    String.format("Call not successful: %d - %s", response.getCode(), response.getMessage())
                );
            }

            return response.getBody();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Thread Error executing OAuth request", e);
            throw new WikipediaException("Thread Error executing OAuth request");
        } catch (ExecutionException | IOException | IllegalArgumentException | NullPointerException e) {
            LOGGER.error("Error executing OAuth request", e);
            throw new WikipediaException("Error executing OAuth request");
        }
    }

    private Verb convertVerb(WikipediaApiVerb verb) {
        return Verb.valueOf(verb.name());
    }

    private OAuth1AccessToken convertAccessToken(AccessToken accessToken) {
        return new OAuth1AccessToken(accessToken.getToken(), accessToken.getTokenSecret());
    }

    private WikipediaApiResponse convert(String responseBody) throws WikipediaException {
        LOGGER.trace("OAuth response body: {}", responseBody);
        try {
            WikipediaApiResponse apiResponse = jsonMapper.readValue(responseBody, WikipediaApiResponse.class);
            apiResponse.validate();
            return apiResponse;
        } catch (JsonProcessingException e) {
            LOGGER.error("Error converting Wikipedia API response: {}", responseBody, e);
            throw new WikipediaException("Error converting Wikipedia API response");
        }
    }
}
