package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.finder.FinderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("default")
class WikipediaServiceImpl implements WikipediaService {

    private static final String MISSPELLING_LIST_PAGE = "Wikipedia:Corrector_ortográfico/Listado";
    private static final String FALSE_POSITIVE_LIST_PAGE = "Usuario:Benjavalero/FalsePositives";
    private static final String COMPOSED_MISSPELLING_LIST_PAGE = "Usuario:Benjavalero/ComposedMisspellings";
    private static final String EDIT_SUMMARY = "Correcciones ortográficas con [[Usuario:Benjavalero/Replacer|Replacer]] (herramienta en línea de revisión de errores)";
    private static final int MAX_PAGES_REQUESTED = 50;
    private static final String PARAM_ACTION = "action";
    private static final String VALUE_QUERY = "query";
    private static final String PARAM_PAGE_ID = "pageid";
    private static final String PARAM_PAGE_IDS = "pageids";

    @Autowired
    private WikipediaRequestService wikipediaRequestService;

    @Value("${replacer.admin.user}")
    private String adminUser;

    @TestOnly
    void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    @Override
    public String getLoggedUserName(OAuth1AccessToken accessToken) throws WikipediaException {
        LOGGER.info("START Get name of the logged user from Wikipedia API. Token: {}", accessToken.getToken());
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeSignedGetRequest(buildUserNameRequestParams(), accessToken);
        String username = extractUserNameFromJson(apiResponse);
        LOGGER.info("END Get name of the logged user from Wikipedia API: {}", username);
        return username;
    }

    private Map<String, String> buildUserNameRequestParams() {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("meta", "userinfo");
        return params;
    }

    private String extractUserNameFromJson(WikipediaApiResponse response) {
        return response.getQuery().getUserinfo().getName();
    }

    @Override
    public boolean isAdminUser(String username) {
        return this.adminUser.equals(username);
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
    public String getComposedMisspellingListPageContent() throws WikipediaException {
        return getPageByTitle(COMPOSED_MISSPELLING_LIST_PAGE)
                .orElseThrow(WikipediaException::new)
                .getContent();
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
        LOGGER.info("START Get page by ID: {}", pageId);
        // Return the only value that should be in the map
        Optional<WikipediaPage> page = getPagesByIds(Collections.singletonList(pageId)).stream().findAny();
        LOGGER.info("END Get page by ID: {} - {}", pageId, page.map(WikipediaPage::getTitle).orElse(""));
        return page;
    }

    @Override
    public List<WikipediaPage> getPagesByIds(List<Integer> pageIds) throws WikipediaException {
        LOGGER.info("START Get pages by a list of IDs: {}", pageIds);
        List<WikipediaPage> pages = new ArrayList<>(pageIds.size());
        // There is a maximum number of pages to request
        // We split the request in several sub-lists
        int start = 0;
        while (start < pageIds.size()) {
            List<Integer> subList = pageIds.subList(start, start + Math.min(pageIds.size() - start, MAX_PAGES_REQUESTED));
            pages.addAll(getPagesByIds(PARAM_PAGE_IDS, StringUtils.join(subList, "|")));
            start += subList.size();
        }
        LOGGER.info("END Get pages by a list of IDs: {}", pages.size());
        return pages;
    }

    private List<WikipediaPage> getPagesByIds(String pagesParam, String pagesValue) throws WikipediaException {
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(buildPageIdsRequestParams(pagesParam, pagesValue));
        return extractPagesFromJson(apiResponse);
    }

    private Map<String, String> buildPageIdsRequestParams(String pagesParam, String pagesValue) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("prop", "revisions");
        params.put("rvprop", "timestamp|content");
        params.put("rvslots", "main");
        params.put(pagesParam, pagesValue);
        params.put("curtimestamp", "true");
        return params;
    }

    private List<WikipediaPage> extractPagesFromJson(WikipediaApiResponse response) {
        // Query timestamp
        String queryTimestamp = response.getCurtimestamp();
        return response.getQuery().getPages().stream()
                .filter(page -> !page.isMissing())
                .map(page -> convertToDto(page, queryTimestamp))
                .collect(Collectors.toList());
    }

    private WikipediaPage convertToDto(WikipediaApiResponse.Page page, String queryTimestamp) {
        return WikipediaPage.builder()
                .id(page.getPageid())
                .title(page.getTitle())
                .namespace(WikipediaNamespace.valueOf(page.getNs()))
                .content(page.getRevisions().get(0).getSlots().getMain().getContent())
                .lastUpdate(WikipediaPage.parseWikipediaTimestamp(page.getRevisions().get(0).getTimestamp()))
                .queryTimestamp(queryTimestamp)
                .build();
    }

    @Override
    public List<WikipediaSection> getPageSections(int pageId) throws WikipediaException {
        LOGGER.info("START Get page sections. Page ID: {}", pageId);
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(buildPageSectionsRequestParams(pageId));
        List<WikipediaSection> sections = extractSectionsFromJson(apiResponse);
        LOGGER.info("END Get page sections. Items found: {}", sections.size());
        return sections;
    }

    private Map<String, String> buildPageSectionsRequestParams(int pageId) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, "parse");
        params.put(PARAM_PAGE_ID, Integer.toString(pageId));
        params.put("prop", "sections");
        return params;
    }

    private List<WikipediaSection> extractSectionsFromJson(WikipediaApiResponse response) {
        return response.getParse().getSections().stream()
                .filter(section -> section.getByteoffset() != null)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private WikipediaSection convertToDto(WikipediaApiResponse.Section section) {
        return WikipediaSection.builder()
                .level(Integer.parseInt(section.getLevel()))
                .index(Integer.parseInt(section.getIndex()))
                .byteOffset(section.getByteoffset())
                .build();
    }

    @Override
    public Optional<WikipediaPage> getPageByIdAndSection(int pageId, int section) throws WikipediaException {
        LOGGER.info("START Get page by ID and section: {} - {}", pageId, section);
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(buildPageIdsAndSectionRequestParams(pageId, section));
        List<WikipediaPage> pages = extractPagesFromJson(apiResponse);
        Optional<WikipediaPage> page = pages.stream().findAny().map(p -> p.withSection(section));
        LOGGER.info("END Get page by ID and section: {} - {} - {}",
                pageId, section, page.map(WikipediaPage::getTitle).orElse(""));
        return page;
    }

    private Map<String, String> buildPageIdsAndSectionRequestParams(int pageId, int section) {
        Map<String, String> params = buildPageIdsRequestParams(PARAM_PAGE_IDS, Integer.toString(pageId));
        params.put("rvsection", Integer.toString(section));
        return params;
    }

    @Override
    public Set<Integer> getPageIdsByStringMatch(String text) throws WikipediaException {
        LOGGER.info("START Get pages by string match: {}", text);
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(buildPageIdsByStringMatchRequestParams(text));
        Set<Integer> pageIds = extractPageIdsFromSearchJson(apiResponse);
        LOGGER.info("END Get pages by string match. Items found: {}", pageIds.size());
        return pageIds;
    }

    private Map<String, String> buildPageIdsByStringMatchRequestParams(String text) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("list", "search");
        params.put("utf8", "1");
        params.put("srlimit", "100");
        params.put("srsearch", buildSearchExpression(text));
        params.put("srnamespace", StringUtils.join(WikipediaNamespace.getProcessableNamespaces().stream()
                .map(WikipediaNamespace::getValue).collect(Collectors.toList()), "|"));
        params.put("srwhat", "text");
        params.put("srinfo", "");
        params.put("srprop", "");
        return params;
    }

    String buildSearchExpression(String text) {
        String quoted = String.format("\"%s\"", text);
        if (FinderUtils.containsUppercase(text)) {
            // Case-sensitive search with a regex
            return String.format("%s insource:/%s/", quoted, quoted);
        } else {
            return quoted;
        }
    }

    private Set<Integer> extractPageIdsFromSearchJson(WikipediaApiResponse response) {
        return response.getQuery().getSearch().stream()
                .map(WikipediaApiResponse.Page::getPageid)
                .collect(Collectors.toSet());
    }

    @Override
    public void savePageContent(int pageId, String pageContent, @Nullable Integer section, String currentTimestamp,
                                OAuth1AccessToken accessToken) throws WikipediaException {
        LOGGER.info("START Save page content into Wikipedia. Page ID: {}", pageId);

        EditToken editToken = getEditToken(pageId, accessToken);
        // Pre-check of edit conflicts
        if (currentTimestamp.compareTo(editToken.getTimestamp()) <= 0) {
            throw new WikipediaException("El artículo ha sido editado por otra persona. Recargue la página para revisar el artículo de nuevo.");
        }

        wikipediaRequestService.executeSignedPostRequest(
                buildSavePageContentRequestParams(pageId, pageContent, section, currentTimestamp, editToken),
                accessToken);
        LOGGER.info("END Save page content into Wikipedia. Page ID: {}", pageId);
    }

    private Map<String, String> buildSavePageContentRequestParams(int pageId, String pageContent, @Nullable Integer section,
                                                                  String currentTimestamp, EditToken editToken) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, "edit");
        params.put(PARAM_PAGE_ID, Integer.toString(pageId));
        params.put("text", pageContent);
        if (section != null) {
            params.put("section", Integer.toString(section));
        }
        params.put("summary", EDIT_SUMMARY);
        params.put("bot", "true");
        params.put("minor", "true");
        params.put("token", editToken.getCsrfToken());
        params.put("starttimestamp", currentTimestamp); // Timestamp when the editing process began
        params.put("basetimestamp", editToken.getTimestamp()); // Timestamp of the base revision
        return params;
    }

    EditToken getEditToken(int pageId, OAuth1AccessToken accessToken) throws WikipediaException {
        LOGGER.debug("START Get edit token. Access Token: {}", accessToken.getToken());
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeSignedPostRequest(
                buildEditTokenRequestParams(pageId), accessToken);
        EditToken editToken = extractEditTokenFromJson(apiResponse);
        LOGGER.debug("END Get edit token: {}", editToken);
        return editToken;
    }

    private Map<String, String> buildEditTokenRequestParams(int pageId) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("meta", "tokens");
        params.put(PARAM_PAGE_IDS, Integer.toString(pageId));
        params.put("prop", "revisions");
        params.put("rvprop", "timestamp");
        return params;
    }

    private EditToken extractEditTokenFromJson(WikipediaApiResponse response) {
        return EditToken.of(response.getQuery().getTokens().getCsrftoken(),
                response.getQuery().getPages().get(0).getRevisions().get(0).getTimestamp());
    }

}
