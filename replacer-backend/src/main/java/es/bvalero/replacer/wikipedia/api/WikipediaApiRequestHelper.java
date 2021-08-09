package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Helper to perform Wikipedia API requests */
@Slf4j
@Component
class WikipediaApiRequestHelper {

    private static final String WIKIPEDIA_API_URL = "https://%s.wikipedia.org/w/api.php";

    @Autowired
    private OAuthService oAuthService;

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

        String url = buildWikipediaRequestUrl(request.getLang());
        String responseBody;
        if (request.getAccessToken() == null) {
            responseBody = oAuthService.executeRequest(request.getVerb().toString(), url, request.getParams());
        } else {
            responseBody =
                oAuthService.executeSignedRequest(
                    request.getVerb().toString(),
                    url,
                    request.getParams(),
                    request.getAccessToken()
                );
        }
        return convert(responseBody);
    }

    private String buildWikipediaRequestUrl(WikipediaLanguage lang) {
        return String.format(WIKIPEDIA_API_URL, lang.getCode());
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
