package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthService;
import es.bvalero.replacer.wikipedia.OAuthToken;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Helper to perform Wikipedia API requests */
@Slf4j
@Component
class WikipediaRequestHelper {

    private static final String VERB_GET = "GET";
    private static final String VERB_POST = "POST";
    private static final String WIKIPEDIA_API_URL = "https://%s.wikipedia.org/w/api.php";

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private ObjectMapper jsonMapper;

    WikipediaApiResponse executeGetRequest(Map<String, String> params, WikipediaLanguage lang)
        throws ReplacerException {
        return executeRequest(params, VERB_GET, lang, null);
    }

    WikipediaApiResponse executeSignedGetRequest(
        Map<String, String> params,
        WikipediaLanguage lang,
        OAuthToken accessToken
    ) throws ReplacerException {
        return executeRequest(params, VERB_GET, lang, accessToken);
    }

    WikipediaApiResponse executeSignedPostRequest(
        Map<String, String> params,
        WikipediaLanguage lang,
        OAuthToken accessToken
    ) throws ReplacerException {
        return executeRequest(params, VERB_POST, lang, accessToken);
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    private WikipediaApiResponse executeRequest(
        Map<String, String> params,
        String verb,
        WikipediaLanguage lang,
        @Nullable OAuthToken accessToken
    ) throws ReplacerException {
        // Add common parameters to receive a JSON response from Wikipedia API
        Map<String, String> parameters = new HashMap<>(params);
        parameters.put("format", "json");
        parameters.put("formatversion", "2");

        String url = buildWikipediaRequestUrl(lang);
        String responseBody;
        if (accessToken == null) {
            responseBody = oAuthService.executeRequest(verb, url, parameters);
        } else {
            responseBody = oAuthService.executeSignedRequest(verb, url, parameters, accessToken);
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
