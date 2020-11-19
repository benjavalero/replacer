package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.ReplacerException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class WikipediaRequestService {
    private static final String WIKIPEDIA_API_URL = "https://%s.wikipedia.org/w/api.php";

    @Autowired
    private OAuth10aService oAuthService;

    @Autowired
    private ObjectMapper jsonMapper;

    WikipediaApiResponse executeGetRequest(Map<String, String> params, WikipediaLanguage lang)
        throws ReplacerException {
        return executeRequest(params, Verb.GET, lang, null);
    }

    WikipediaApiResponse executeSignedGetRequest(
        Map<String, String> params,
        WikipediaLanguage lang,
        @Nullable OAuth1AccessToken accessToken
    )
        throws ReplacerException {
        return executeRequest(params, Verb.GET, lang, accessToken);
    }

    WikipediaApiResponse executeSignedPostRequest(
        Map<String, String> params,
        WikipediaLanguage lang,
        @Nullable OAuth1AccessToken accessToken
    )
        throws ReplacerException {
        return executeRequest(params, Verb.POST, lang, accessToken);
    }

    private WikipediaApiResponse executeRequest(
        Map<String, String> params,
        Verb verb,
        WikipediaLanguage lang,
        @Nullable OAuth1AccessToken accessToken
    )
        throws ReplacerException {
        boolean isSigned = accessToken != null && StringUtils.isNotBlank(accessToken.getToken());
        LOGGER.debug("START Execute OAuth Request. Params: {} - Verb: {} - Signed: {}", params, verb, isSigned);
        OAuthRequest request = createOAuthRequestWithParams(params, verb, lang);

        if (isSigned) {
            signOAuthRequest(request, accessToken);
        }

        try {
            Response response = oAuthService.execute(request);
            if (!response.isSuccessful()) {
                throw new ReplacerException(
                    String.format("Call not successful: %d - %s", response.getCode(), response.getMessage())
                );
            }

            LOGGER.debug("END Execute OAuth Request. Response Body: {}", response.getBody());
            WikipediaApiResponse apiResponse = jsonMapper.readValue(response.getBody(), WikipediaApiResponse.class);
            validateApiResponse(apiResponse);
            return apiResponse;
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

    private void signOAuthRequest(OAuthRequest request, OAuth1AccessToken accessToken) {
        oAuthService.signRequest(accessToken, request);
    }

    private void validateApiResponse(WikipediaApiResponse response) throws ReplacerException {
        if (response.getError() != null) {
            String code = response.getError().getCode();
            String info = response.getError().getInfo();
            throw new ReplacerException(String.format("%s: %s", code, info));
        }
    }
}
