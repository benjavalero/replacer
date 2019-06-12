package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.AuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

// We make this implementation public to be used by the finder benchmarks
@Service
@Profile("default")
public class WikipediaServiceImpl implements WikipediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikipediaServiceImpl.class);
    private static final String MISSPELLING_LIST_PAGE = "Wikipedia:Corrector_ortográfico/Listado";
    private static final String FALSE_POSITIVE_LIST_PAGE = "Usuario:Benjavalero/FalsePositives";
    private static final String EDIT_SUMMARY = "Correcciones ortográficas";
    private static final int MAX_PAGES_REQUESTED = 50;
    private static final String PARAM_ACTION = "action";

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public Optional<WikipediaPage> getPageByTitle(String pageTitle) throws WikipediaException {
        LOGGER.info("Find Wikipedia page by title: {}", pageTitle);
        // Return the only value that should be in the map
        return getPagesByIds("titles", pageTitle).values().stream().findFirst();
    }

    @Override
    public Optional<WikipediaPage> getPageById(int pageId) throws WikipediaException {
        LOGGER.info("Find Wikipedia page by ID: {}", pageId);
        // Return the only value that should be in the map
        return getPagesByIds(Collections.singletonList(pageId)).values().stream().findFirst();
    }

    @Override
    public Map<Integer, WikipediaPage> getPagesByIds(List<Integer> pageIds)
            throws WikipediaException {
        Map<Integer, WikipediaPage> pageContents = new HashMap<>(pageIds.size());
        // There is a maximum number of pages to request
        // We split the request in several sub-lists
        int start = 0;
        while (start < pageIds.size()) {
            List<Integer> subList = pageIds.subList(start, start + Math.min(pageIds.size() - start, MAX_PAGES_REQUESTED));
            pageContents.putAll(getPagesByIds("pageids", StringUtils.join(subList, "|")));
            start += subList.size();
        }
        return pageContents;
    }

    private Map<Integer, WikipediaPage> getPagesByIds(String pagesParam, String pagesValue) throws WikipediaException {
        try {
            return extractPagesFromApiResponse(
                    authenticationService.executeUnsignedOAuthRequest(getParamsToRequestPages(pagesParam, pagesValue)));
        } catch (AuthenticationException e) {
            throw new WikipediaException("Error getting Wikipedia pages", e);
        }
    }

    private Map<String, String> getParamsToRequestPages(String pagesParam, String pagesValue) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, "query");
        params.put("prop", "revisions");
        params.put("rvprop", "timestamp|content");
        params.put("rvslots", "main");
        params.put(pagesParam, pagesValue);
        return params;
    }

    private Map<Integer, WikipediaPage> extractPagesFromApiResponse(JsonNode json) throws WikipediaException {
        JsonNode jsonError = json.get("error");
        if (jsonError != null) {
            String errorMsg = String.format("%s: %s", jsonError.get("code").asText(), jsonError.get("info").asText());
            throw new WikipediaException(errorMsg);
        }

        Map<Integer, WikipediaPage> pageContents = new HashMap<>();
        json.at("/query/pages").forEach(jsonPage -> {
            JsonNode jsonContent = jsonPage.at("/revisions/0/slots/main/content");
            // There may be no content if the page is missing
            if (!jsonContent.isMissingNode()) {
                int pageId = jsonPage.get("pageid").asInt();
                WikipediaPage page = WikipediaPage.builder()
                        .setId(pageId)
                        .setTitle(jsonPage.get("title").asText())
                        .setNamespace(jsonPage.get("ns").asInt())
                        .setContent(jsonContent.asText())
                        .setTimestamp(jsonPage.at("/revisions/0/timestamp").asText())
                        .build();
                pageContents.put(pageId, page);
            }
        });
        return pageContents;
    }

    @Override
    public void savePageContent(int pageId, String pageContent, LocalDateTime editTime, OAuth1AccessToken accessToken)
            throws WikipediaException {
        LOGGER.info("Save page content into Wikipedia");
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, "edit");
        params.put("pageid", Integer.toString(pageId));
        params.put("text", pageContent);
        params.put("summary", EDIT_SUMMARY);
        params.put("minor", "true");
        params.put("token", getEditToken(accessToken));

        // TODO : Save the reviewer
        // TODO : Take into account conflicts during the edition
        // TODO : Test saving when session has expired in frontend
        try {
            authenticationService.executeOAuthRequest(params, accessToken);
        } catch (AuthenticationException e) {
            throw new WikipediaException(e);
        }
    }

    String getEditToken(OAuth1AccessToken accessToken) throws WikipediaException {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, "query");
        params.put("meta", "tokens");

        try {
            JsonNode jsonResponse = authenticationService.executeOAuthRequest(params, accessToken);
            return jsonResponse.at("/query/tokens/csrftoken").asText();
        } catch (AuthenticationException e) {
            throw new WikipediaException("Error getting edit token", e);
        }
    }

    @Override
    public String getMisspellingListPageContent() throws WikipediaException {
        return getPageByTitle(MISSPELLING_LIST_PAGE)
                .orElseThrow(WikipediaException::new)
                .getContent();
    }

    @Override
    public String getFalsePositiveListPageContent() throws WikipediaException {
        return getPageByTitle(FALSE_POSITIVE_LIST_PAGE)
                .orElseThrow(WikipediaException::new)
                .getContent();
    }

}
