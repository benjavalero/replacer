package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.FinderUtils;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!offline")
class WikipediaServiceImpl implements WikipediaService {
    private static final String EDIT_SUMMARY =
        "Correcciones ortográficas con [[Usuario:Benjavalero/Replacer|Replacer]] (herramienta en línea de revisión de errores)";
    private static final int MAX_PAGES_REQUESTED = 50;
    private static final String PARAM_ACTION = "action";
    private static final String VALUE_QUERY = "query";
    private static final String PARAM_PAGE_ID = "pageid";
    private static final String PARAM_PAGE_IDS = "pageids";

    @Autowired
    private WikipediaRequestService wikipediaRequestService;

    @Autowired
    private ModelMapper modelMapper;

    @Setter
    @Value("${replacer.admin.user}")
    private String adminUser;

    @Resource
    private Map<String, String> simpleMisspellingPages;

    @Resource
    private Map<String, String> composedMisspellingPages;

    @Resource
    private Map<String, String> falsePositivePages;

    @Override
    public String getLoggedUserName(OAuth1AccessToken accessToken, WikipediaLanguage lang) throws ReplacerException {
        LOGGER.info("START Get name of the logged user from Wikipedia API. Token: {}", accessToken.getToken());
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeSignedGetRequest(
            buildUserNameRequestParams(),
            lang,
            accessToken
        );
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
    public String getMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return getPageByTitle(simpleMisspellingPages.get(lang.getCode()), lang)
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getFalsePositiveListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return getPageByTitle(falsePositivePages.get(lang.getCode()), lang)
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public String getComposedMisspellingListPageContent(WikipediaLanguage lang) throws ReplacerException {
        return getPageByTitle(composedMisspellingPages.get(lang.getCode()), lang)
            .orElseThrow(ReplacerException::new)
            .getContent();
    }

    @Override
    public Optional<WikipediaPage> getPageByTitle(String pageTitle, WikipediaLanguage lang) throws ReplacerException {
        LOGGER.info("START Find Wikipedia page by title: {}", pageTitle);
        // Return the only value that should be in the map
        Optional<WikipediaPage> page = getPagesByIds("titles", pageTitle, lang).stream().findAny();
        LOGGER.info("END Find Wikipedia page by title: {}", pageTitle);
        return page;
    }

    @Override
    public Optional<WikipediaPage> getPageById(int pageId, WikipediaLanguage lang) throws ReplacerException {
        LOGGER.debug("START Get page by ID: {}", pageId);
        // Return the only value that should be in the map
        Optional<WikipediaPage> page = getPagesByIds(Collections.singletonList(pageId), lang).stream().findAny();
        LOGGER.debug("END Get page by ID: {} - {}", pageId, page.map(WikipediaPage::getTitle).orElse(""));
        return page;
    }

    @Override
    public List<WikipediaPage> getPagesByIds(List<Integer> pageIds, WikipediaLanguage lang) throws ReplacerException {
        LOGGER.debug("START Get pages by a list of IDs: {}", pageIds);
        List<WikipediaPage> pages = new ArrayList<>(pageIds.size());
        // There is a maximum number of pages to request
        // We split the request in several sub-lists
        int start = 0;
        while (start < pageIds.size()) {
            List<Integer> subList = pageIds.subList(
                start,
                start + Math.min(pageIds.size() - start, MAX_PAGES_REQUESTED)
            );
            pages.addAll(getPagesByIds(PARAM_PAGE_IDS, StringUtils.join(subList, "|"), lang));
            start += subList.size();
        }
        LOGGER.debug("END Get pages by a list of IDs: {}", pages.size());
        return pages;
    }

    private List<WikipediaPage> getPagesByIds(String pagesParam, String pagesValue, WikipediaLanguage lang)
        throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(
            buildPageIdsRequestParams(pagesParam, pagesValue),
            lang
        );
        return extractPagesFromJson(apiResponse).stream().map(page -> page.withLang(lang)).collect(Collectors.toList());
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
        return response
            .getQuery()
            .getPages()
            .stream()
            .filter(page -> !page.isMissing())
            .map(page -> convertToDto(page, queryTimestamp))
            .collect(Collectors.toList());
    }

    private WikipediaPage convertToDto(WikipediaApiResponse.Page page, String queryTimestamp) {
        return WikipediaPage
            .builder()
            .id(page.getPageid())
            .title(page.getTitle())
            .namespace(WikipediaNamespace.valueOf(page.getNs()))
            .content(page.getRevisions().get(0).getSlots().getMain().getContent())
            .lastUpdate(WikipediaPage.parseWikipediaTimestamp(page.getRevisions().get(0).getTimestamp()))
            .queryTimestamp(queryTimestamp)
            .build();
    }

    @Override
    public List<WikipediaSection> getPageSections(int pageId, WikipediaLanguage lang) throws ReplacerException {
        LOGGER.debug("START Get page sections. Page ID: {}", pageId);
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(
            buildPageSectionsRequestParams(pageId),
            lang
        );
        List<WikipediaSection> sections = extractSectionsFromJson(apiResponse);
        LOGGER.debug("END Get page sections. Items found: {}", sections.size());
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
        return response
            .getParse()
            .getSections()
            .stream()
            .filter(this::isSectionValid)
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private boolean isSectionValid(WikipediaApiResponse.Section section) {
        try {
            return Integer.parseInt(section.getIndex()) > 0 && section.getByteoffset() != null;
        } catch (NumberFormatException nfe) {
            LOGGER.debug("Invalid page section: {}", section);
            return false;
        }
    }

    private WikipediaSection convertToDto(WikipediaApiResponse.Section section) {
        return modelMapper.map(section, WikipediaSection.class);
    }

    @Override
    public Optional<WikipediaPage> getPageByIdAndSection(int pageId, WikipediaSection section, WikipediaLanguage lang)
        throws ReplacerException {
        LOGGER.debug("START Get page by ID and section: {} - {}", pageId, section);
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(
            buildPageIdsAndSectionRequestParams(pageId, section.getIndex()),
            lang
        );
        List<WikipediaPage> pages = extractPagesFromJson(apiResponse);
        Optional<WikipediaPage> page = pages
            .stream()
            .findAny()
            .map(p -> p.withLang(lang).withSection(section.getIndex()).withAnchor(section.getAnchor()));
        LOGGER.debug(
            "END Get page by ID and section: {} - {} - {}",
            pageId,
            section,
            page.map(WikipediaPage::getTitle).orElse("")
        );
        return page;
    }

    private Map<String, String> buildPageIdsAndSectionRequestParams(int pageId, int section) {
        Map<String, String> params = buildPageIdsRequestParams(PARAM_PAGE_IDS, Integer.toString(pageId));
        params.put("rvsection", Integer.toString(section));
        return params;
    }

    @Override
    public PageSearchResult getPageIdsByStringMatch(String text, int offset, int limit, WikipediaLanguage lang)
        throws ReplacerException {
        LOGGER.info("START Get pages by string match: {}", text);
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(
            buildPageIdsByStringMatchRequestParams(text, offset, limit),
            lang
        );
        PageSearchResult pageIds = extractPageIdsFromSearchJson(apiResponse);
        LOGGER.info(
            "END Get pages by string match. Items found: {}/{}",
            pageIds.getPageIds().size(),
            pageIds.getTotal()
        );
        return pageIds;
    }

    private Map<String, String> buildPageIdsByStringMatchRequestParams(String text, int offset, int limit) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("list", "search");
        params.put("utf8", "1");
        params.put("srlimit", Integer.toString(limit));
        params.put("sroffset", Integer.toString(offset));
        params.put("srsearch", buildSearchExpression(text));
        params.put(
            "srnamespace",
            StringUtils.join(
                WikipediaNamespace
                    .getProcessableNamespaces()
                    .stream()
                    .map(WikipediaNamespace::getValue)
                    .collect(Collectors.toList()),
                "|"
            )
        );
        params.put("srwhat", "text");
        params.put("srinfo", "totalhits");
        params.put("srprop", "");
        return params;
    }

    @VisibleForTesting
    String buildSearchExpression(String text) {
        String quoted = String.format("\"%s\"", text);
        if (FinderUtils.containsUppercase(text)) {
            // Case-sensitive search with a regex
            return String.format("%s insource:/%s/", quoted, quoted);
        } else {
            return quoted;
        }
    }

    private PageSearchResult extractPageIdsFromSearchJson(WikipediaApiResponse response) {
        return new PageSearchResult(
            response.getQuery().getSearchinfo().getTotalhits(),
            response
                .getQuery()
                .getSearch()
                .stream()
                .map(WikipediaApiResponse.Page::getPageid)
                .collect(Collectors.toList())
        );
    }

    @Override
    public void savePageContent(
        int pageId,
        String pageContent,
        @Nullable Integer section,
        String currentTimestamp,
        WikipediaLanguage lang,
        OAuth1AccessToken accessToken
    )
        throws ReplacerException {
        LOGGER.info("START Save page content into Wikipedia. Page ID: {}", pageId);

        EditToken editToken = getEditToken(pageId, lang, accessToken);
        // Pre-check of edit conflicts
        if (currentTimestamp.compareTo(editToken.getTimestamp()) <= 0) {
            throw new ReplacerException(
                "El artículo ha sido editado por otra persona. Recargue la página para revisar el artículo de nuevo."
            );
        }

        wikipediaRequestService.executeSignedPostRequest(
            buildSavePageContentRequestParams(pageId, pageContent, section, currentTimestamp, editToken),
            lang,
            accessToken
        );
        LOGGER.info("END Save page content into Wikipedia. Page ID: {}", pageId);
    }

    private Map<String, String> buildSavePageContentRequestParams(
        int pageId,
        String pageContent,
        @Nullable Integer section,
        String currentTimestamp,
        EditToken editToken
    ) {
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

    @VisibleForTesting
    EditToken getEditToken(int pageId, WikipediaLanguage lang, OAuth1AccessToken accessToken) throws ReplacerException {
        LOGGER.debug("START Get edit token. Access Token: {}", accessToken.getToken());
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeSignedPostRequest(
            buildEditTokenRequestParams(pageId),
            lang,
            accessToken
        );
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
        return EditToken.of(
            response.getQuery().getTokens().getCsrftoken(),
            response.getQuery().getPages().get(0).getRevisions().get(0).getTimestamp()
        );
    }
}
