package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
class WikipediaRequestService {

    private static final String WIKIPEDIA_API_URL = "https://es.wikipedia.org/w/api.php";

    @Autowired
    private OAuth10aService oAuthService;

    @Autowired
    private ObjectMapper jsonMapper;

    JsonNode executeGetRequest(Map<String, String> params) throws WikipediaException {
        return executeRequest(params, Verb.GET, null);
    }

    JsonNode executeSignedGetRequest(Map<String, String> params, @Nullable OAuth1AccessToken accessToken)
            throws WikipediaException {
        return executeRequest(params, Verb.GET, accessToken);
    }

    JsonNode executeSignedPostRequest(Map<String, String> params, @Nullable OAuth1AccessToken accessToken)
            throws WikipediaException {
        return executeRequest(params, Verb.POST, accessToken);
    }

    private JsonNode executeRequest(Map<String, String> params, Verb verb, @Nullable OAuth1AccessToken accessToken)
            throws WikipediaException {
        boolean isSigned = accessToken != null && StringUtils.isNotBlank(accessToken.getToken());
        LOGGER.debug("START Execute OAuth Request. Params: {} - Verb: {} - Signed: {}", params, verb, isSigned);
        OAuthRequest request = createOAuthRequestWithParams(params, verb);

        if (isSigned) {
            signOAuthRequest(request, accessToken);
        }

        try {
            Response response = oAuthService.execute(request);
            if (!response.isSuccessful()) {
                throw new WikipediaException(String.format("Call not successful: %d - %s",
                        response.getCode(), response.getMessage()));
            }

            LOGGER.debug("END Execute OAuth Request. Response Body: {}", response.getBody());
            JsonNode json = jsonMapper.readTree(response.getBody());
            validateJsonResponse(json);
            return json;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WikipediaException("ERROR executing OAuth Request", e);
        } catch (ExecutionException | IOException | NullPointerException e) {
            throw new WikipediaException("ERROR executing OAuth Request", e);
        }
    }

    private OAuthRequest createOAuthRequestWithParams(Map<String, String> params, Verb verb) {
        OAuthRequest request = new OAuthRequest(verb, WIKIPEDIA_API_URL);

        // Add request parameter
        params.forEach(request::addParameter);

        // Add common parameters to receive a JSON response from Wikipedia API
        request.addParameter("format", "json");
        request.addParameter("formatversion", "2");

        return request;
    }

    private void signOAuthRequest(OAuthRequest request, OAuth1AccessToken accessToken) {
        oAuthService.signRequest(accessToken, request);
    }

    private void validateJsonResponse(JsonNode json) throws WikipediaException {
        JsonNode jsonError = json.get("error");
        if (jsonError != null) {
            String errorMsg = String.format("%s: %s", jsonError.get("code").asText(), jsonError.get("info").asText());
            throw new WikipediaException(errorMsg);
        }
    }

}
