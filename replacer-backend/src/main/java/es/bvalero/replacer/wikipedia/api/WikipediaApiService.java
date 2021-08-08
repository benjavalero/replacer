package es.bvalero.replacer.wikipedia.api;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.*;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** Wikipedia service implementation using classic Wikipedia API */
@Slf4j
@Service
@Profile("!offline")
class WikipediaApiService implements WikipediaService {

    private static final int MAX_PAGES_REQUESTED = 50;
    private static final String PARAM_ACTION = "action";
    private static final String VALUE_QUERY = "query";
    private static final String PARAM_PAGE_ID = "pageid";
    private static final String PARAM_PAGE_IDS = "pageids";
    private static final int MAX_OFFSET_LIMIT = 10000;

    @Autowired
    private WikipediaRequestService wikipediaRequestService;

    @Value("${replacer.processable.namespaces}")
    private Set<Integer> processableNamespaces;

    @Override
    public UserInfo getUserInfo(WikipediaLanguage lang, OAuthToken accessToken) throws ReplacerException {
        return convertUserInfo(getLoggedUserName(lang, accessToken));
    }

    private UserInfo convertUserInfo(WikipediaApiResponse.UserInfo userInfo) {
        return UserInfo.of(userInfo.getName(), userInfo.getGroups());
    }

    @VisibleForTesting
    @Loggable(value = Loggable.DEBUG)
    WikipediaApiResponse.UserInfo getLoggedUserName(WikipediaLanguage lang, OAuthToken accessToken)
        throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeSignedGetRequest(
            buildUserNameRequestParams(),
            lang,
            accessToken
        );
        return extractUserNameFromJson(apiResponse);
    }

    private Map<String, String> buildUserNameRequestParams() {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("meta", "userinfo");
        params.put("uiprop", "groups");
        return params;
    }

    private WikipediaApiResponse.UserInfo extractUserNameFromJson(WikipediaApiResponse response) {
        return response.getQuery().getUserinfo();
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @Override
    public Optional<WikipediaPage> getPageByTitle(WikipediaLanguage lang, String pageTitle) throws ReplacerException {
        // Return the only value that should be in the map
        return getPagesByIds("titles", pageTitle, lang).stream().findAny();
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @Override
    public Optional<WikipediaPage> getPageById(WikipediaLanguage lang, int pageId) throws ReplacerException {
        // Return the only value that should be in the map
        return getPagesByIds(Collections.singletonList(pageId), lang).stream().findAny();
    }

    @VisibleForTesting
    @Loggable(prepend = true, value = Loggable.TRACE)
    List<WikipediaPage> getPagesByIds(List<Integer> pageIds, WikipediaLanguage lang) throws ReplacerException {
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
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(
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
            .map(page -> convert(page, lang, queryTimestamp))
            .collect(Collectors.toList());
    }

    private WikipediaPage convert(WikipediaApiResponse.Page page, WikipediaLanguage lang, String queryTimestamp) {
        return WikipediaPage
            .builder()
            .lang(lang)
            .id(page.getPageid())
            .namespace(WikipediaNamespace.valueOf(page.getNs()))
            .title(page.getTitle())
            .content(page.getRevisions().get(0).getSlots().getMain().getContent())
            .lastUpdate(WikipediaDateUtils.parseWikipediaTimestamp(page.getRevisions().get(0).getTimestamp()))
            .queryTimestamp(WikipediaDateUtils.parseWikipediaTimestamp(queryTimestamp))
            .build();
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @Override
    public List<WikipediaSection> getPageSections(WikipediaLanguage lang, int pageId) throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(
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
            .map(this::convert)
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

    private WikipediaSection convert(WikipediaApiResponse.Section section) {
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
    public Optional<WikipediaPage> getPageByIdAndSection(WikipediaLanguage lang, int pageId, WikipediaSection section)
        throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(
            buildPageIdsAndSectionRequestParams(pageId, section.getIndex()),
            lang
        );
        List<WikipediaPage> pages = extractPagesFromJson(apiResponse, lang);
        return pages.stream().findAny().map(p -> p.withSection(section));
    }

    private Map<String, String> buildPageIdsAndSectionRequestParams(int pageId, int section) {
        Map<String, String> params = buildPageIdsRequestParams(PARAM_PAGE_IDS, Integer.toString(pageId));
        params.put("rvsection", Integer.toString(section));
        return params;
    }

    @Override
    @Loggable(value = Loggable.DEBUG, prepend = true)
    public WikipediaSearchResult getPageIdsByStringMatch(
        WikipediaLanguage lang,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) throws ReplacerException {
        // Avoid exception when reaching the maximum offset limit
        if (offset + limit >= MAX_OFFSET_LIMIT) {
            LOGGER.warn("Maximum offset reached: {} - {} - {}", lang, text, caseSensitive);
            return WikipediaSearchResult.ofEmpty();
        }

        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(
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
        params.put("srnamespace", StringUtils.join(processableNamespaces, "|"));
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

    private WikipediaSearchResult extractPageIdsFromSearchJson(WikipediaApiResponse response) {
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

        return WikipediaSearchResult.of(response.getQuery().getSearchinfo().getTotalhits(), pageIds);
    }

    @Override
    public void savePageContent(
        WikipediaLanguage lang,
        int pageId,
        @Nullable Integer section,
        String pageContent,
        LocalDateTime currentTimestamp,
        String editSummary,
        OAuthToken accessToken
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

        wikipediaRequestService.executeSignedPostRequest(
            buildSavePageContentRequestParams(pageId, pageContent, section, currentTimestamp, editSummary, editToken),
            lang,
            accessToken
        );
    }

    private Map<String, String> buildSavePageContentRequestParams(
        int pageId,
        String pageContent,
        @Nullable Integer section,
        LocalDateTime currentTimestamp,
        String editSummary,
        EditToken editToken
    ) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, "edit");
        params.put(PARAM_PAGE_ID, Integer.toString(pageId));
        params.put("text", pageContent);
        if (section != null) {
            params.put("section", Integer.toString(section));
        }
        params.put("summary", editSummary);
        params.put("watchlist", "nochange");
        params.put("bot", "true");
        params.put("minor", "true");
        params.put("token", editToken.getCsrfToken());
        // Timestamp when the editing process began
        params.put("starttimestamp", WikipediaDateUtils.formatWikipediaTimestamp(currentTimestamp));
        // Timestamp of the base revision
        params.put("basetimestamp", WikipediaDateUtils.formatWikipediaTimestamp(editToken.getTimestamp()));
        return params;
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @VisibleForTesting
    EditToken getEditToken(int pageId, WikipediaLanguage lang, OAuthToken accessToken) throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeSignedPostRequest(
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
            WikipediaDateUtils.parseWikipediaTimestamp(
                response.getQuery().getPages().get(0).getRevisions().get(0).getTimestamp()
            )
        );
    }
}
