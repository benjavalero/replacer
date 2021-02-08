package es.bvalero.replacer.wikipedia;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
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
    private static final int MAX_OFFSET_LIMIT = 10000;

    @Autowired
    private WikipediaApiFacade wikipediaApiFacade;

    @Setter // For testing
    @Value("${replacer.admin.user}")
    private String adminUser;

    @Resource
    private Map<String, String> simpleMisspellingPages;

    @Resource
    private Map<String, String> composedMisspellingPages;

    @Resource
    private Map<String, String> falsePositivePages;

    @Override
    public RequestToken getRequestToken() throws ReplacerException {
        return wikipediaApiFacade.getRequestToken();
    }

    @Override
    public WikipediaUser getLoggedUser(String requestToken, String requestTokenSecret, String oauthVerifier)
        throws ReplacerException {
        AccessToken accessToken = this.getAccessToken(requestToken, requestTokenSecret, oauthVerifier);
        String userName = this.getLoggedUserName(accessToken);
        boolean admin = this.isAdminUser(userName);
        return WikipediaUser.of(userName, admin, accessToken);
    }

    private AccessToken getAccessToken(String requestToken, String requestTokenSecret, String oauthVerifier)
        throws ReplacerException {
        return wikipediaApiFacade.getAccessToken(requestToken, requestTokenSecret, oauthVerifier);
    }

    @VisibleForTesting
    String getLoggedUserName(AccessToken accessToken) throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaApiFacade.executeSignedGetRequest(
            buildUserNameRequestParams(),
            WikipediaLanguage.getDefault(),
            accessToken
        );
        return extractUserNameFromJson(apiResponse);
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

    @VisibleForTesting
    boolean isAdminUser(String username) {
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

    @Loggable(prepend = true, value = Loggable.TRACE)
    @Override
    public Optional<WikipediaPage> getPageByTitle(String pageTitle, WikipediaLanguage lang) throws ReplacerException {
        // Return the only value that should be in the map
        return getPagesByIds("titles", pageTitle, lang).stream().findAny();
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @Override
    public Optional<WikipediaPage> getPageById(int pageId, WikipediaLanguage lang) throws ReplacerException {
        // Return the only value that should be in the map
        return getPagesByIds(Collections.singletonList(pageId), lang).stream().findAny();
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @Override
    public List<WikipediaPage> getPagesByIds(List<Integer> pageIds, WikipediaLanguage lang) throws ReplacerException {
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
        return pages;
    }

    private List<WikipediaPage> getPagesByIds(String pagesParam, String pagesValue, WikipediaLanguage lang)
        throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaApiFacade.executeGetRequest(
            buildPageIdsRequestParams(pagesParam, pagesValue),
            lang
        );
        return extractPagesFromJson(apiResponse, lang);
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

    private List<WikipediaPage> extractPagesFromJson(WikipediaApiResponse response, WikipediaLanguage lang) {
        // Query timestamp
        String queryTimestamp = response.getCurtimestamp();
        return response
            .getQuery()
            .getPages()
            .stream()
            .filter(page -> !page.isMissing())
            .map(page -> convertToDto(page, queryTimestamp, lang))
            .collect(Collectors.toList());
    }

    private WikipediaPage convertToDto(WikipediaApiResponse.Page page, String queryTimestamp, WikipediaLanguage lang) {
        return WikipediaPage
            .builder()
            .id(page.getPageid())
            .lang(lang)
            .title(page.getTitle())
            .namespace(WikipediaNamespace.valueOf(page.getNs()))
            .content(page.getRevisions().get(0).getSlots().getMain().getContent())
            .lastUpdate(WikipediaUtils.parseWikipediaTimestamp(page.getRevisions().get(0).getTimestamp()))
            .queryTimestamp(queryTimestamp)
            .build();
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @Override
    public List<WikipediaSection> getPageSections(int pageId, WikipediaLanguage lang) throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaApiFacade.executeGetRequest(
            buildPageSectionsRequestParams(pageId),
            lang
        );
        return extractSectionsFromJson(apiResponse);
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
            LOGGER.warn("Invalid page section: {}", section);
            return false;
        }
    }

    private WikipediaSection convertToDto(WikipediaApiResponse.Section section) {
        return WikipediaSection
            .builder()
            .level(Integer.parseInt(section.getLevel()))
            .index(Integer.parseInt(section.getIndex()))
            .byteOffset(section.getByteoffset())
            .anchor(section.getAnchor())
            .build();
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @Override
    public Optional<WikipediaPage> getPageByIdAndSection(int pageId, WikipediaSection section, WikipediaLanguage lang)
        throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaApiFacade.executeGetRequest(
            buildPageIdsAndSectionRequestParams(pageId, section.getIndex()),
            lang
        );
        List<WikipediaPage> pages = extractPagesFromJson(apiResponse, lang);
        return pages.stream().findAny().map(p -> p.withSection(section.getIndex()).withAnchor(section.getAnchor()));
    }

    private Map<String, String> buildPageIdsAndSectionRequestParams(int pageId, int section) {
        Map<String, String> params = buildPageIdsRequestParams(PARAM_PAGE_IDS, Integer.toString(pageId));
        params.put("rvsection", Integer.toString(section));
        return params;
    }

    @Override
    @Loggable(value = Loggable.DEBUG)
    public PageSearchResult getPageIdsByStringMatch(
        WikipediaLanguage lang,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) throws ReplacerException {
        // Avoid exception when reaching the maximum offset limit
        if (offset + limit >= MAX_OFFSET_LIMIT) {
            LOGGER.warn("Maximum offset reached: {} - {} - {}", lang, text, caseSensitive);
            return PageSearchResult.ofEmpty();
        }

        WikipediaApiResponse apiResponse = wikipediaApiFacade.executeGetRequest(
            buildPageIdsByStringMatchRequestParams(text, caseSensitive, offset, limit),
            lang
        );
        return extractPageIdsFromSearchJson(apiResponse);
    }

    private Map<String, String> buildPageIdsByStringMatchRequestParams(
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("list", "search");
        params.put("utf8", "1");
        params.put("srlimit", Integer.toString(limit));
        params.put("sroffset", Integer.toString(offset));
        params.put("srsort", "create_timestamp_asc"); // So the order is invariable after editing
        params.put("srsearch", buildSearchExpression(text, caseSensitive));
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

    @Loggable(value = Loggable.DEBUG)
    @VisibleForTesting
    String buildSearchExpression(String text, boolean caseSensitive) {
        // Search directly in the source is very expensive
        // Wikipedia recommends searching the term as usual
        // and then narrow the results with "insource"
        String quoted = String.format("\"%s\"", text);
        if (caseSensitive) {
            // Case-sensitive search with a regex
            quoted = String.format("%s insource:/%s/", quoted, quoted);
        }
        return quoted;
    }

    private PageSearchResult extractPageIdsFromSearchJson(WikipediaApiResponse response) {
        List<Integer> pageIds = response
            .getQuery()
            .getSearch()
            .stream()
            .map(WikipediaApiResponse.Page::getPageid)
            .collect(Collectors.toList());

        // Check nullity of IDs just in case to try to reproduce a strange bug
        if (pageIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Null page ID in API response: " + response);
        }

        return PageSearchResult.of(response.getQuery().getSearchinfo().getTotalhits(), pageIds);
    }

    @Loggable(value = Loggable.DEBUG, ignore = ReplacerException.class)
    @Override
    public void savePageContent(
        WikipediaLanguage lang,
        int pageId,
        @Nullable Integer section,
        String pageContent,
        String currentTimestamp,
        AccessToken accessToken
    ) throws ReplacerException {
        EditToken editToken = getEditToken(pageId, lang, accessToken);
        // Pre-check of edit conflicts
        if (currentTimestamp.compareTo(editToken.getTimestamp()) <= 0) {
            LOGGER.warn(
                "Page edited at the same time: {} - {} - {} - {} - {}",
                currentTimestamp,
                editToken.getTimestamp(),
                lang,
                pageId,
                pageContent
            );
            throw new ReplacerException(
                "Esta página de Wikipedia ha sido editada por otra persona. Recargue para revisarla de nuevo."
            );
        }

        wikipediaApiFacade.executeSignedPostRequest(
            buildSavePageContentRequestParams(pageId, pageContent, section, currentTimestamp, editToken),
            lang,
            accessToken
        );
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
        params.put("watchlist", "nochange");
        params.put("bot", "true");
        params.put("minor", "true");
        params.put("token", editToken.getCsrfToken());
        params.put("starttimestamp", currentTimestamp); // Timestamp when the editing process began
        params.put("basetimestamp", editToken.getTimestamp()); // Timestamp of the base revision
        return params;
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @VisibleForTesting
    EditToken getEditToken(int pageId, WikipediaLanguage lang, AccessToken accessToken) throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaApiFacade.executeSignedPostRequest(
            buildEditTokenRequestParams(pageId),
            lang,
            accessToken
        );
        return extractEditTokenFromJson(apiResponse);
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
