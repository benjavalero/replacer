package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.AuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// We make this implementation public to be used by the finder benchmarks
@Service
@Profile("default")
public class WikipediaServiceImpl implements WikipediaService {

    private static final String MISSPELLING_LIST_PAGE = "Wikipedia:Corrector_ortográfico/Listado";
    private static final String FALSE_POSITIVE_LIST_PAGE = "Usuario:Benjavalero/FalsePositives";
    private static final String EDIT_SUMMARY = "Correcciones ortográficas";
    private static final int MAX_PAGES_REQUESTED = 50;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public String getPageContent(String pageTitle) throws WikipediaException {
        return getPageContent(pageTitle, null);
    }

    @Override
    public String getPageContent(String pageTitle, @Nullable OAuth1AccessToken accessToken)
            throws WikipediaException {
        // Return the only value that should be in the map
        Map<Integer, String> contents = getPagesContent("titles", pageTitle, accessToken);
        if (contents.size() > 0) {
            return new ArrayList<>(contents.values()).get(0);
        } else {
            throw new UnavailablePageException();
        }
    }

    @Override
    public Map<Integer, String> getPagesContent(List<Integer> pageIds, @Nullable OAuth1AccessToken accessToken)
            throws WikipediaException {
        Map<Integer, String> pageContents = new HashMap<>(pageIds.size());
        // There is a maximum number of pages to request
        // We split the request in several sub-lists
        int start = 0;
        while (start < pageIds.size()) {
            List<Integer> subList = pageIds.subList(start, start + Math.min(pageIds.size() - start, MAX_PAGES_REQUESTED));
            pageContents.putAll(getPagesContent("pageids", StringUtils.join(subList, "|"), accessToken));
            start += subList.size();
        }
        return pageContents;
    }

    private Map<Integer, String> getPagesContent(String pagesParam, String pagesValue, @Nullable OAuth1AccessToken accessToken)
            throws WikipediaException {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("prop", "revisions");
        params.put("rvprop", "content");
        params.put("rvslots", "main");
        params.put(pagesParam, pagesValue);

        try {
            JsonNode jsonResponse = authenticationService.executeOAuthRequest(params, accessToken);
            return extractPagesContentFromApiResponse(jsonResponse);
        } catch (AuthenticationException e) {
            throw new WikipediaException("Error getting page content", e);
        }
    }

    private Map<Integer, String> extractPagesContentFromApiResponse(JsonNode json) throws WikipediaException {
        JsonNode jsonError = json.get("error");
        if (jsonError != null) {
            String errorMsg = String.format("%s: %s", jsonError.get("code").asText(), jsonError.get("info").asText());
            throw new WikipediaException(errorMsg);
        }

        Map<Integer, String> pageContents = new HashMap<>();
        json.at("/query/pages").forEach(page -> {
            JsonNode jsonContent = page.at("/revisions/0/slots/main/content");
            // There may be no content if the page is missing
            if (!jsonContent.isMissingNode()) {
                int pageId = page.get("pageid").asInt();
                pageContents.put(pageId, jsonContent.asText());
            }
        });
        return pageContents;
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

        try {
            authenticationService.executeOAuthRequest(params, accessToken);
        } catch (AuthenticationException e) {
            throw new WikipediaException(e);
        }
    }

    String getEditToken(OAuth1AccessToken accessToken) throws WikipediaException {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("meta", "tokens");

        try {
            JsonNode jsonResponse = authenticationService.executeOAuthRequest(params, accessToken);
            return jsonResponse.at("/query/tokens/csrftoken").asText();
        } catch (AuthenticationException e) {
            throw new WikipediaException("Error getting edit token", e);
        }
    }

    public String getMisspellingListPageContent() throws WikipediaException {
        return getPageContent(MISSPELLING_LIST_PAGE);
    }

    public String getFalsePositiveListPageContent() throws WikipediaException {
        return getPageContent(FALSE_POSITIVE_LIST_PAGE);
    }

}
