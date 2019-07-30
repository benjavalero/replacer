package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// We make this implementation public to be used by the finder benchmarks
@Slf4j
@Service
@Profile("default")
public class WikipediaServiceImpl implements WikipediaService {

    private static final String WIKIPEDIA_API_URL = "https://es.wikipedia.org/w/api.php";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final String MISSPELLING_LIST_PAGE = "Wikipedia:Corrector_ortográfico/Listado";
    private static final String FALSE_POSITIVE_LIST_PAGE = "Usuario:Benjavalero/FalsePositives";
    private static final String EDIT_SUMMARY = "Correcciones ortográficas";
    private static final int MAX_PAGES_REQUESTED = 50;
    private static final String PARAM_ACTION = "action";
    private static final String VALUE_QUERY = "query";
    private static final String PARAM_PAGE_ID = "pageid";

    @Autowired
    private AuthenticationService authenticationService;

    private JsonNode executeWikipediaApiRequest(Map<String, String> params, boolean post, OAuth1AccessToken accessToken)
            throws WikipediaException {
        String response = null;
        try {
            // Add standard parameters to receive a JSON response fro Wikipedia API
            params.put("format", "json");
            params.put("formatversion", "2");

            response = authenticationService.executeOAuthRequest(WIKIPEDIA_API_URL, params, post, accessToken);
            JsonNode json = JSON_MAPPER.readTree(response);
            handleErrorsInJsonResponse(json);
            return json;
        } catch (AuthenticationException e) {
            LOGGER.error("Error authenticating wit Wikipedia API", e);
            throw new WikipediaException("Error authenticating wit Wikipedia API");
        } catch (IOException e) {
            LOGGER.error("Error handling response from Wikipedia: {}", response, e);
            throw new WikipediaException(String.format("Error handling response from Wikipedia: %s", response));
        }
    }

    private void handleErrorsInJsonResponse(JsonNode json) throws WikipediaException {
        JsonNode jsonError = json.get("error");
        if (jsonError != null) {
            String errorMsg = String.format("%s: %s", jsonError.get("code").asText(), jsonError.get("info").asText());
            throw new WikipediaException(errorMsg);
        }
    }

    @Override
    public Optional<WikipediaPage> getPageByTitle(String pageTitle) throws WikipediaException {
        LOGGER.info("START Find Wikipedia page by title: {}", pageTitle);
        // Return the only value that should be in the map
        Optional<WikipediaPage> page = getPagesByIds("titles", pageTitle).stream().findAny();
        LOGGER.info("END Find Wikipedia page by title: {}", pageTitle);
        return page;
    }

    @Override
    public Optional<WikipediaPage> getPageById(int pageId) throws WikipediaException {
        LOGGER.info("START Find Wikipedia page by ID: {}", pageId);
        // Return the only value that should be in the map
        Optional<WikipediaPage> page = getPagesByIds(Collections.singletonList(pageId)).stream().findAny();
        LOGGER.info("END Find Wikipedia page by ID: {}", pageId);
        return page;
    }

    // We make this method public to be used by the finder benchmarks
    @Override
    public List<WikipediaPage> getPagesByIds(List<Integer> pageIds) throws WikipediaException {
        List<WikipediaPage> pageContents = new ArrayList<>(pageIds.size());
        // There is a maximum number of pages to request
        // We split the request in several sub-lists
        int start = 0;
        while (start < pageIds.size()) {
            List<Integer> subList = pageIds.subList(start, start + Math.min(pageIds.size() - start, MAX_PAGES_REQUESTED));
            pageContents.addAll(getPagesByIds("pageids", StringUtils.join(subList, "|")));
            start += subList.size();
        }
        return pageContents;
    }

    @Override
    public Set<Integer> getPageIdsByStringMatch(String text) throws WikipediaException {
        LOGGER.info("START Find Wikipedia pages by string match: {}", text);

        // Parameters
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("list", "search");
        params.put("utf8", "1");
        params.put("srlimit", "100");
        params.put("srsearch", String.format("\"%s\"", text));
        params.put("srnamespace", StringUtils.join(WikipediaNamespace.getProcessableNamespaces().stream()
                .map(WikipediaNamespace::getValue).collect(Collectors.toList()), "|"));
        params.put("srwhat", "text");
        params.put("srinfo", "");
        params.put("srprop", "");

        JsonNode jsonResponse = executeWikipediaApiRequest(params, false, null);
        Set<Integer> pageIds = extractPageIdsFromApiResponse(jsonResponse);
        LOGGER.info("END Find Wikipedia pages by string match. Items found: {}", pageIds.size());
        return pageIds;
    }

    private List<WikipediaPage> getPagesByIds(String pagesParam, String pagesValue) throws WikipediaException {
        return extractPagesFromApiResponse(executeWikipediaApiRequest(
                getParamsToRequestPages(pagesParam, pagesValue), false, null));
    }

    private Map<String, String> getParamsToRequestPages(String pagesParam, String pagesValue) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("prop", "revisions");
        params.put("rvprop", "timestamp|content");
        params.put("rvslots", "main");
        params.put(pagesParam, pagesValue);
        params.put("curtimestamp", "true");
        return params;
    }

    private List<WikipediaPage> extractPagesFromApiResponse(JsonNode json) {
        // Query timestamp
        String queryTimestamp = json.at("/curtimestamp").asText();

        List<WikipediaPage> pageContents = new ArrayList<>();
        json.at("/query/pages").forEach(jsonPage -> {
            JsonNode jsonContent = jsonPage.at("/revisions/0/slots/main/content");
            // There may be no content if the page is missing
            if (!jsonContent.isMissingNode()) {
                int pageId = jsonPage.get(PARAM_PAGE_ID).asInt();
                WikipediaPage page = WikipediaPage.builder()
                        .setId(pageId)
                        .setTitle(jsonPage.get("title").asText())
                        .setNamespace(jsonPage.get("ns").asInt())
                        .setContent(jsonContent.asText())
                        .setTimestamp(jsonPage.at("/revisions/0/timestamp").asText())
                        .setQueryTimestamp(queryTimestamp)
                        .build();
                pageContents.add(page);
            }
        });
        return pageContents;
    }

    private Set<Integer> extractPageIdsFromApiResponse(JsonNode json) {
        return StreamSupport.stream(json.at("/query/search").spliterator(), false)
                .map(page -> page.get(PARAM_PAGE_ID).asInt()).collect(Collectors.toSet());
    }

    @Override
    public void savePageContent(int pageId, String pageContent, String currentTimestamp, OAuth1AccessToken accessToken)
            throws WikipediaException {
        LOGGER.info("START Save page content into Wikipedia. Page ID: {}", pageId);

        EditToken editToken = getEditToken(pageId, accessToken);
        // Pre-check of edit conflicts
        if (currentTimestamp.compareTo(editToken.getTimestamp()) <= 0) {
            throw new WikipediaException("El artículo ha sido editado por otra persona. Recargue la página para revisar el artículo de nuevo.");
        }

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, "edit");
        params.put(PARAM_PAGE_ID, Integer.toString(pageId));
        params.put("text", pageContent);
        params.put("summary", EDIT_SUMMARY);
        params.put("minor", "true");
        params.put("token", editToken.getCsrftoken());
        params.put("starttimestamp", currentTimestamp); // Timestamp when the editing process began
        params.put("basetimestamp", editToken.getTimestamp()); // Timestamp of the base revision

        executeWikipediaApiRequest(params, true, accessToken);
        LOGGER.info("END Save page content into Wikipedia. Page ID: {}", pageId);
    }

    EditToken getEditToken(int pageId, OAuth1AccessToken accessToken) throws WikipediaException {
        LOGGER.debug("START Get edit token. Access Token: {}", accessToken.getToken());
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("meta", "tokens");
        params.put("pageids", Integer.toString(pageId));
        params.put("prop", "revisions");
        params.put("rvprop", "timestamp");

        JsonNode jsonResponse = executeWikipediaApiRequest(params, true, accessToken);
        EditToken editToken = new EditToken(
                jsonResponse.at("/query/tokens/csrftoken").asText(),
                jsonResponse.at("/query/pages/0/revisions/0/timestamp").asText()
        );
        LOGGER.debug("END Get edit token: {}", editToken);
        return editToken;
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

    @Override
    public String identify(OAuth1AccessToken accessToken) throws WikipediaException {
        LOGGER.info("START Get name of the logged user from Wikipedia API. Token: {}", accessToken.getToken());
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("meta", "userinfo");

        JsonNode jsonResponse = executeWikipediaApiRequest(params, false, accessToken);
        String username = jsonResponse.at("/query/userinfo/name").asText();
        LOGGER.info("END Get name of the logged user from Wikipedia API: {}", username);
        return username;
    }

}
