package es.bvalero.replacer.wikipedia;

import static org.apache.commons.lang3.StringUtils.SPACE;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequestHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequestVerb;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** Wikipedia service implementation using classic Wikipedia API */
@Slf4j
@Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 10, warnUnit = TimeUnit.SECONDS)
@Service
@Profile("!offline")
class WikipediaPageApiRepository implements WikipediaPageRepository {

    private static final int MAX_PAGES_REQUESTED = 50; // MediaWiki API allows to retrieve the content of maximum 50 pages
    private static final int MAX_SEARCH_RESULTS = 500; // MediaWiki API allows at most 500 pages in a search result
    private static final String PARAM_ACTION = "action";
    private static final String VALUE_QUERY = "query";
    private static final String PARAM_PAGE_ID = "pageid";
    private static final String PARAM_PAGE_IDS = "pageids";
    private static final int MAX_OFFSET_LIMIT = 10000;

    @Autowired
    private WikipediaApiRequestHelper wikipediaApiRequestHelper;

    @Override
    public Optional<WikipediaPage> findByTitle(WikipediaLanguage lang, String pageTitle) {
        try {
            // Return the only value that should be in the map
            return getPagesByIds("titles", pageTitle, lang).stream().findAny();
        } catch (WikipediaException e) {
            LOGGER.error("Error finding page by title: {} - {}", lang, pageTitle, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<WikipediaPage> findByKey(PageKey pageKey) {
        // Return the only value that should be in the map
        return findByKeys(List.of(pageKey)).stream().findAny();
    }

    @Loggable(value = LogLevel.TRACE, skipArgs = true, skipResult = true, warnOver = 10, warnUnit = TimeUnit.SECONDS)
    @Override
    public Collection<WikipediaPage> findByKeys(Collection<PageKey> pageKeys) {
        if (pageKeys.stream().map(PageKey::getLang).distinct().count() > 1) {
            throw new IllegalArgumentException("All pages have to share the same language");
        }

        List<WikipediaPage> pages = new ArrayList<>(pageKeys.size());
        // There is a maximum number of pages to request
        // We split the request in several sub-lists
        try {
            WikipediaLanguage lang = pageKeys.stream().map(PageKey::getLang).distinct().findAny().orElseThrow();
            List<Integer> idList = pageKeys.stream().map(PageKey::getPageId).collect(Collectors.toList());
            int start = 0;
            while (start < idList.size()) {
                List<Integer> subList = idList.subList(
                    start,
                    start + Math.min(idList.size() - start, MAX_PAGES_REQUESTED)
                );
                pages.addAll(getPagesByIds(PARAM_PAGE_IDS, StringUtils.join(subList, '|'), lang));
                start += subList.size();
            }
        } catch (Exception e) {
            LOGGER.error("Error finding pages by ID: {}", StringUtils.join(pageKeys, SPACE), e);
        }
        return pages;
    }

    private Collection<WikipediaPage> getPagesByIds(String pagesParam, String pagesValue, WikipediaLanguage lang)
        throws WikipediaException {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.GET)
            .lang(lang)
            .params(buildPageIdsRequestParams(pagesParam, pagesValue))
            .build();
        WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
        return extractPagesFromJson(apiResponse, lang);
    }

    private Map<String, String> buildPageIdsRequestParams(String pagesParam, String pagesValue) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("prop", "info|revisions");
        params.put("inprop", "protection");
        params.put("rvprop", "timestamp|content");
        params.put("rvslots", "main");
        params.put(pagesParam, pagesValue);
        params.put("curtimestamp", "true");
        return params;
    }

    private Collection<WikipediaPage> extractPagesFromJson(WikipediaApiResponse response, WikipediaLanguage lang) {
        // Query timestamp
        String queryTimestamp = response.getCurtimestamp();
        return response
            .getQuery()
            .getPages()
            .stream()
            .filter(page -> !page.isMissing())
            .filter(page -> !page.isProtected())
            .map(page -> convert(page, lang, queryTimestamp))
            .collect(Collectors.toUnmodifiableList());
    }

    private WikipediaPage convert(WikipediaApiResponse.Page page, WikipediaLanguage lang, String queryTimestamp) {
        return WikipediaPage
            .builder()
            .pageKey(PageKey.of(lang, page.getPageid()))
            .namespace(WikipediaNamespace.valueOf(page.getNs()))
            .title(page.getTitle())
            .content(page.getRevisions().get(0).getSlots().getMain().getContent())
            .lastUpdate(WikipediaDateUtils.parseWikipediaTimestamp(page.getRevisions().get(0).getTimestamp()))
            .queryTimestamp(WikipediaDateUtils.parseWikipediaTimestamp(queryTimestamp))
            .redirect(page.isRedirect())
            .build();
    }

    @Override
    public Collection<WikipediaSection> findSectionsInPage(PageKey pageKey) {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.GET)
            .lang(pageKey.getLang())
            .params(buildPageSectionsRequestParams(pageKey.getPageId()))
            .build();
        try {
            WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
            return extractSectionsFromJson(apiResponse);
        } catch (WikipediaException e) {
            LOGGER.error("Error finding sections in page: {}", pageKey, e);
        }
        return Collections.emptyList();
    }

    private Map<String, String> buildPageSectionsRequestParams(int pageId) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, "parse");
        params.put(PARAM_PAGE_ID, Integer.toString(pageId));
        params.put("prop", "sections");
        return params;
    }

    private Collection<WikipediaSection> extractSectionsFromJson(WikipediaApiResponse response) {
        return response
            .getParse()
            .getSections()
            .stream()
            .filter(this::isSectionValid)
            .map(this::convert)
            .collect(Collectors.toUnmodifiableList());
    }

    private boolean isSectionValid(WikipediaApiResponse.Section section) {
        try {
            return Integer.parseInt(section.getIndex()) > 0 && section.getByteoffset() != null;
        } catch (NumberFormatException nfe) {
            LOGGER.info("Invalid page section: {}", section);
            return false;
        }
    }

    private WikipediaSection convert(WikipediaApiResponse.Section section) {
        return WikipediaSection
            .builder()
            .level(Integer.parseInt(section.getLevel()))
            .index(Integer.parseInt(section.getIndex()))
            .byteOffset(section.getByteoffset())
            .anchor(Objects.requireNonNullElse(section.getLinkAnchor(), section.getAnchor()))
            .build();
    }

    @Override
    public Optional<WikipediaPage> findPageSection(PageKey pageKey, WikipediaSection section) {
        try {
            WikipediaApiRequest apiRequest = WikipediaApiRequest
                .builder()
                .verb(WikipediaApiRequestVerb.GET)
                .lang(pageKey.getLang())
                .params(buildPageIdsAndSectionRequestParams(pageKey.getPageId(), section.getIndex()))
                .build();
            WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
            Collection<WikipediaPage> pages = extractPagesFromJson(apiResponse, pageKey.getLang());
            return pages.stream().findAny();
        } catch (WikipediaException e) {
            LOGGER.error("Error getting page section: {} - {}", pageKey, section, e);
            return Optional.empty();
        }
    }

    private Map<String, String> buildPageIdsAndSectionRequestParams(int pageId, int section) {
        Map<String, String> params = buildPageIdsRequestParams(PARAM_PAGE_IDS, Integer.toString(pageId));
        params.put("rvsection", Integer.toString(section));
        return params;
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public WikipediaSearchResult findByContent(
        WikipediaLanguage lang,
        Collection<WikipediaNamespace> namespaces,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) {
        if (limit > MAX_SEARCH_RESULTS) {
            LOGGER.error("Too big number of results to search: " + limit);
            return WikipediaSearchResult.ofEmpty();
        }

        // Avoid exception when reaching the maximum offset limit
        if (offset + limit >= MAX_OFFSET_LIMIT) {
            LOGGER.warn("Maximum offset reached: {} - {} - {}", lang, text, caseSensitive);
            return WikipediaSearchResult.ofEmpty();
        }

        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.GET)
            .lang(lang)
            .params(buildPageIdsByStringMatchRequestParams(namespaces, text, caseSensitive, offset, limit))
            .build();
        try {
            WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
            return extractPageIdsFromSearchJson(apiResponse);
        } catch (WikipediaException e) {
            LOGGER.error("Error finding pages by content", e);
        }
        return WikipediaSearchResult.ofEmpty();
    }

    private Map<String, String> buildPageIdsByStringMatchRequestParams(
        Collection<WikipediaNamespace> namespaces,
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
        Collection<Integer> namespaceIds = namespaces
            .stream()
            .map(WikipediaNamespace::getValue)
            .collect(Collectors.toUnmodifiableSet());
        params.put("srnamespace", StringUtils.join(namespaceIds, '|'));
        params.put("srwhat", "text");
        params.put("srinfo", "totalhits");
        params.put("srprop", "");
        return params;
    }

    @VisibleForTesting
    String buildSearchExpression(String text, boolean caseSensitive) {
        // Search directly in the source is very expensive
        // Wikipedia recommends searching the term as usual
        // and then narrow the results with "insource"
        // We use a regex for the case-sensitive and a regular quote for case-insensitive
        char delimiter = caseSensitive ? '/' : '"';
        String quoted = String.format("\"%s\" insource:%s%s%s", text, delimiter, text, delimiter);
        LOGGER.debug("Search expression: {} - {} ==> {}", text, caseSensitive, quoted);
        return quoted;
    }

    private WikipediaSearchResult extractPageIdsFromSearchJson(WikipediaApiResponse response) {
        Collection<Integer> pageIds = response
            .getQuery()
            .getSearch()
            .stream()
            .map(WikipediaApiResponse.Page::getPageid)
            .collect(Collectors.toUnmodifiableList());

        // Check nullity of IDs just in case to try to reproduce a strange bug
        if (pageIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Null page ID in API response: " + response);
        }

        return WikipediaSearchResult
            .builder()
            .total(response.getQuery().getSearchinfo().getTotalhits())
            .pageIds(pageIds)
            .build();
    }

    @Override
    public void save(
        PageKey pageKey,
        @Nullable Integer section,
        String content,
        LocalDateTime queryTimestamp,
        String editSummary,
        AccessToken accessToken
    ) throws WikipediaException {
        EditToken editToken = getEditToken(pageKey, accessToken);
        // Pre-check of edit conflicts
        if (!queryTimestamp.isAfter(editToken.getTimestamp())) {
            String message = String.format(
                "Page edited at the same time: %s -%s - %s",
                pageKey,
                queryTimestamp,
                queryTimestamp
            );
            // This exception is always logged by the aspect-based library even when ignored
            // See: https://github.com/rozidan/logger-spring-boot/issues/3
            throw new WikipediaConflictException(message);
        }

        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.POST)
            .lang(pageKey.getLang())
            .params(
                buildSavePageContentRequestParams(
                    pageKey.getPageId(),
                    content,
                    section,
                    queryTimestamp,
                    editSummary,
                    editToken
                )
            )
            .accessToken(accessToken)
            .build();
        wikipediaApiRequestHelper.executeApiRequest(apiRequest);
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

    @VisibleForTesting
    EditToken getEditToken(PageKey id, AccessToken accessToken) throws WikipediaException {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.POST)
            .lang(id.getLang())
            .params(buildEditTokenRequestParams(id.getPageId()))
            .accessToken(accessToken)
            .build();
        WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
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
