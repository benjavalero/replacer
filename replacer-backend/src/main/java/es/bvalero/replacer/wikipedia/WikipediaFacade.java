package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.AuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Profile("default")
public class WikipediaFacade implements IWikipediaFacade {

    private static final String EDIT_SUMMARY = "Correcciones ortográficas";
    private static final int MAX_PAGES_REQUESTED = 50;

    @Autowired
    private AuthenticationService authenticationService;

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
        params.put("rvslots", "main");
        params.put("titles", pageTitle);

        String apiResponse = executeOAuthRequest(params, accessToken);
        return extractPageContentFromApiResponse(parseApiResponse(apiResponse));
    }

    private String extractPageContentFromApiResponse(JsonNode json) {
        String content = null;

        JsonNode pages = json.get("query").get("pages");
        if (pages != null && pages.size() > 0) {
            JsonNode revisions = pages.get(0).get("revisions");
            if (revisions != null && revisions.size() > 0) {
                content = revisions.get(0).get("slots").get("main").get("content").asText();
            }
        }

        return content;
    }

    @Override
    public String getPageContent(int pageId, @Nullable OAuth1AccessToken accessToken) throws WikipediaException {
        Map<Integer, String> pageContents = getPagesContent(Collections.singletonList(pageId), accessToken);
        if (pageContents.containsKey(pageId)) {
            return pageContents.get(pageId);
        } else {
            throw new UnavailablePageException();
        }
    }

    @Override
    public Map<Integer, String> getPagesContent(List<Integer> pageIds, @Nullable OAuth1AccessToken accessToken)
            throws WikipediaException {
        Map<Integer, String> pageContents = new HashMap<>();
        // The maximum number of requested pages is 50
        int start = 0;
        while (pageIds.size() - start >= MAX_PAGES_REQUESTED) {
            List<Integer> subList = pageIds.subList(start, Math.min(pageIds.size(), start + MAX_PAGES_REQUESTED));
            pageContents.putAll(getPagesContentLimited(subList, accessToken));
            start += subList.size();
        }
        return pageContents;
    }

    private Map<Integer, String> getPagesContentLimited(Collection<Integer> pageIds, @Nullable OAuth1AccessToken accessToken)
            throws WikipediaException {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("prop", "revisions");
        params.put("rvprop", "content");
        params.put("rvslots", "main");
        params.put("pageids", StringUtils.join(pageIds, "|"));

        String apiResponse = executeOAuthRequest(params, accessToken);
        return extractPagesContentFromApiResponse(parseApiResponse(apiResponse));
    }

    private Map<Integer, String> extractPagesContentFromApiResponse(JsonNode json) {
        Map<Integer, String> pageContents = new HashMap<>();

        JsonNode pages = json.get("query").get("pages");
        if (pages != null) {
            pages.forEach(page -> {
                if (page.get("pageid") != null && page.get("revisions") != null) {
                    int pageId = page.get("pageid").asInt();
                    String content = page.get("revisions").get(0).get("slots").get("main").get("content").asText();
                    pageContents.put(pageId, content);
                }
            });
        }

        return pageContents;
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
