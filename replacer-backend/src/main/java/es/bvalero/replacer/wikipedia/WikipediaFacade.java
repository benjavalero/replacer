package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.IAuthenticationService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Profile("default")
class WikipediaFacade implements IWikipediaFacade {

    private static final String EDIT_SUMMARY = "Correcciones ortográficas";

    @Autowired
    private IAuthenticationService authenticationService;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getPageContent(String pageTitle) throws WikipediaException {
        return getPageContent(pageTitle, null);
    }

    @Override
    public String getPageContent(String pageTitle, @Nullable OAuth1AccessToken accessToken) throws WikipediaException {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("prop", "revisions");
        params.put("rvprop", "content");
        params.put("titles", pageTitle);

        String apiResponse = executeOAuthRequest(params, accessToken);
        JsonNode json = parseApiResponse(apiResponse);
        JsonNode pages = json.get("query").get("pages");
        if (pages != null && pages.size() > 0) {
            JsonNode page = pages.get(0);
            if (page != null) {
                JsonNode revisions = page.get("revisions");
                if (revisions != null && revisions.size() > 0) {
                    return revisions.get(0).get("content").asText();
                }
            }
        }

        // We arrive here in case no content (and no error/warning) is found
        throw new UnavailablePageException();
    }

    private JsonNode parseApiResponse(String apiResponse) throws WikipediaException {
        try {
            return this.mapper.readTree(apiResponse);
        } catch (IOException e) {
            throw new WikipediaException(e);
        }
    }

    private String executeOAuthRequest(Map<String, String> params, @Nullable OAuth1AccessToken accessToken)
            throws WikipediaException {
        try {
            String apiResponse = accessToken == null
                    ? this.authenticationService.executeOAuthRequest(params)
                    : this.authenticationService.executeAndSignOAuthRequest(params, accessToken);
            checkApiResponse(apiResponse);
            return apiResponse;
        } catch (AuthenticationException e) {
            throw new WikipediaException(e);
        }
    }

    private void checkApiResponse(String apiResponse) throws WikipediaException {
        if (apiResponse == null) {
            throw new WikipediaException("API result is null");
        }

        JsonNode json = parseApiResponse(apiResponse);
        if (json.get("error") != null) {
            throw new WikipediaException(json.get("error").asText());
        } else if (json.get("warnings") != null) {
            throw new WikipediaException(json.get("warnings").asText());
        }
    }

    @Override
    public void savePageContent(String pageTitle, String pageContent, LocalDateTime editTime, OAuth1AccessToken accessToken)
            throws WikipediaException {
        // TODO : Use editTime to check just before uploading there are no changes during the edition
        Map<String, String> params = new HashMap<>();
        params.put("action", "edit");
        params.put("title", pageTitle);
        params.put("text", pageContent);
        params.put("summary", EDIT_SUMMARY);
        params.put("minor", "true");
        params.put("token", getEditToken(accessToken));

        executeOAuthRequest(params, accessToken);
    }

    private String getEditToken(OAuth1AccessToken accessToken) throws WikipediaException {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("meta", "tokens");

        String apiResponse = executeOAuthRequest(params, accessToken);
        JsonNode json = parseApiResponse(apiResponse);
        return json.get("query").get("tokens").get("csrftoken").asText();
    }

}
