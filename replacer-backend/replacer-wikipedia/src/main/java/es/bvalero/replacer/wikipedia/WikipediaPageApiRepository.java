package es.bvalero.replacer.wikipedia;

import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import es.bvalero.replacer.wikipedia.api.WikipediaApiVerb;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@SuppressWarnings("java:S1192")
@Slf4j
@Service
@Profile("!offline")
class WikipediaPageApiRepository implements WikipediaPageRepository {

    private static final int MAX_PAGES_REQUESTED = 50; // MediaWiki API allows to retrieve the content of maximum 50 pages
    private static final int MAX_SEARCH_RESULTS = 500; // MediaWiki API allows at most 500 pages in a search result
    private static final int MAX_OFFSET_LIMIT = 10000;

    @Autowired
    private WikipediaApiHelper wikipediaApiHelper;

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
                pages.addAll(getPagesByIds("pageids", StringUtils.join(subList, '|'), lang));
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
            .verb(WikipediaApiVerb.GET)
            .lang(lang)
            .params(buildPageIdsRequestParams(pagesParam, pagesValue))
            .build();
        WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
        return extractPagesFromJson(apiResponse, lang);
    }

    private Map<String, String> buildPageIdsRequestParams(String pagesParam, String pagesValue) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
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
        String curtimestamp = response.getCurtimestamp();
        return response
            .getQuery()
            .getPages()
            .stream()
            .filter(page -> !page.isMissing())
            .filter(page -> !page.isProtected())
            .map(page -> convert(page, lang, curtimestamp))
            .toList();
    }

    private WikipediaPage convert(WikipediaApiResponse.Page page, WikipediaLanguage lang, String curtimestamp) {
        return WikipediaPage
            .builder()
            .pageKey(PageKey.of(lang, page.getPageid()))
            .namespace(WikipediaNamespace.valueOf(page.getNs()))
            .title(page.getTitle())
            .content(page.getRevisions().stream().findFirst().orElseThrow().getSlots().getMain().getContent())
            .lastUpdate(WikipediaTimestamp.of(page.getRevisions().stream().findFirst().orElseThrow().getTimestamp()))
            .queryTimestamp(WikipediaTimestamp.of(curtimestamp))
            .redirect(page.isRedirect())
            .build();
    }

    @Override
    public Collection<WikipediaSection> findSectionsInPage(PageKey pageKey) {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiVerb.GET)
            .lang(pageKey.getLang())
            .params(buildPageSectionsRequestParams(pageKey.getPageId()))
            .build();
        try {
            WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
            return extractSectionsFromJson(apiResponse, pageKey);
        } catch (WikipediaException e) {
            LOGGER.error("Error finding sections in page: {}", pageKey, e);
        }
        return List.of();
    }

    private Map<String, String> buildPageSectionsRequestParams(int pageId) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "parse");
        params.put("pageid", Integer.toString(pageId));
        params.put("prop", "sections");
        return params;
    }

    private Collection<WikipediaSection> extractSectionsFromJson(WikipediaApiResponse response, PageKey pageKey) {
        return response
            .getParse()
            .getSections()
            .stream()
            .filter(this::isSectionValid)
            .map(s -> convert(s, pageKey))
            .toList();
    }

    private boolean isSectionValid(WikipediaApiResponse.Section section) {
        try {
            return Integer.parseInt(section.getIndex()) > 0 && section.getByteoffset() != null;
        } catch (NumberFormatException nfe) {
            LOGGER.info("Invalid page section: {}", section);
            return false;
        }
    }

    private WikipediaSection convert(WikipediaApiResponse.Section section, PageKey pageKey) {
        return WikipediaSection
            .builder()
            .pageKey(pageKey)
            .index(Integer.parseInt(section.getIndex()))
            .level(Integer.parseInt(section.getLevel()))
            .byteOffset(Objects.requireNonNull(section.getByteoffset()))
            .anchor(Objects.requireNonNullElse(section.getLinkAnchor(), section.getAnchor()))
            .build();
    }

    @Override
    public Optional<WikipediaPage> findPageSection(WikipediaSection section) {
        try {
            PageKey pageKey = section.getPageKey();
            WikipediaApiRequest apiRequest = WikipediaApiRequest
                .builder()
                .verb(WikipediaApiVerb.GET)
                .lang(pageKey.getLang())
                .params(buildPageIdsAndSectionRequestParams(pageKey.getPageId(), section.getIndex()))
                .build();
            WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
            Collection<WikipediaPage> pages = extractPagesFromJson(apiResponse, pageKey.getLang());
            return pages.stream().findAny();
        } catch (WikipediaException e) {
            LOGGER.error("Error getting page section: {}", section, e);
            return Optional.empty();
        }
    }

    private Map<String, String> buildPageIdsAndSectionRequestParams(int pageId, int section) {
        Map<String, String> params = buildPageIdsRequestParams("pageids", Integer.toString(pageId));
        params.put("rvsection", Integer.toString(section));
        return params;
    }

    @Override
    public WikipediaSearchResult findByContent(WikipediaSearchRequest searchRequest) {
        LOGGER.debug("START Find Page by Content: {}", searchRequest);

        if (searchRequest.getLimit() > MAX_SEARCH_RESULTS) {
            LOGGER.error("Too big number of results to search: " + searchRequest.getLimit());
            return WikipediaSearchResult.ofEmpty();
        }

        // Avoid exception when reaching the maximum offset limit
        if (searchRequest.getOffset() + searchRequest.getLimit() >= MAX_OFFSET_LIMIT) {
            LOGGER.warn(
                "Maximum offset reached: {} - {} - {}",
                searchRequest.getLang(),
                searchRequest.getText(),
                searchRequest.isCaseSensitive()
            );
            return WikipediaSearchResult.ofEmpty();
        }

        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiVerb.GET)
            .lang(searchRequest.getLang())
            .params(buildPageIdsByStringMatchRequestParams(searchRequest))
            .build();
        WikipediaSearchResult result = WikipediaSearchResult.ofEmpty();
        try {
            WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
            result = extractPageIdsFromSearchJson(apiResponse);
        } catch (WikipediaException e) {
            LOGGER.error("Error finding pages by content", e);
        }
        LOGGER.debug("END Find Page by Content: {}", result);
        return result;
    }

    private Map<String, String> buildPageIdsByStringMatchRequestParams(WikipediaSearchRequest searchRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("list", "search");
        params.put("utf8", "1");
        params.put("srlimit", Integer.toString(searchRequest.getLimit()));
        params.put("sroffset", Integer.toString(searchRequest.getOffset()));
        params.put("srsort", "create_timestamp_asc"); // So the order is invariable after editing
        params.put("srsearch", buildSearchExpression(searchRequest.getText(), searchRequest.isCaseSensitive()));
        Collection<Integer> namespaceIds = searchRequest
            .getNamespaces()
            .stream()
            .map(WikipediaNamespace::getValue)
            .collect(Collectors.toUnmodifiableSet());
        if (!namespaceIds.isEmpty()) {
            params.put("srnamespace", StringUtils.join(namespaceIds, '|'));
        }
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
            .map(WikipediaApiResponse.Search::getPageid)
            .toList();

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
    public void save(WikipediaPageSave pageSave, AccessToken accessToken) throws WikipediaException {
        EditToken editToken = getEditToken(pageSave.getPageKey(), accessToken);
        validateEditTimestamp(pageSave.getPageKey(), editToken.getTimestamp(), pageSave.getQueryTimestamp());

        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiVerb.POST)
            .lang(pageSave.getPageKey().getLang())
            .params(buildSavePageContentRequestParams(pageSave, editToken))
            .accessToken(accessToken)
            .build();
        wikipediaApiHelper.executeApiRequest(apiRequest);
    }

    private void validateEditTimestamp(
        PageKey pageKey,
        WikipediaTimestamp editTimestamp,
        WikipediaTimestamp queryTimestamp
    ) throws WikipediaConflictException {
        // Pre-check of edit conflicts
        if (queryTimestamp.isBeforeOrEquals(editTimestamp)) {
            String message = String.format(
                "Page edited at the same time: %s - %s - %s",
                pageKey,
                queryTimestamp,
                editTimestamp
            );
            throw new WikipediaConflictException(message);
        }
    }

    private Map<String, String> buildSavePageContentRequestParams(WikipediaPageSave pageSave, EditToken editToken) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "edit");
        params.put("pageid", Integer.toString(pageSave.getPageKey().getPageId()));
        params.put("text", pageSave.getContent());
        if (pageSave.getSectionId() != null) {
            params.put("section", Integer.toString(pageSave.getSectionId()));
        }
        params.put("summary", pageSave.getEditSummary());
        params.put("watchlist", "nochange");
        params.put("bot", "true");
        params.put("minor", "true");
        params.put("token", editToken.getCsrfToken());
        // Timestamp when the editing process began
        params.put("starttimestamp", pageSave.getQueryTimestamp().toString());
        // Timestamp of the base revision
        params.put("basetimestamp", editToken.getTimestamp().toString());
        return params;
    }

    @VisibleForTesting
    EditToken getEditToken(PageKey id, AccessToken accessToken) throws WikipediaException {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiVerb.POST)
            .lang(id.getLang())
            .params(buildEditTokenRequestParams(id.getPageId()))
            .accessToken(accessToken)
            .build();
        WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
        return extractEditTokenFromJson(apiResponse);
    }

    private Map<String, String> buildEditTokenRequestParams(int pageId) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("meta", "tokens");
        params.put("pageids", Integer.toString(pageId));
        params.put("prop", "revisions");
        params.put("rvprop", "timestamp");
        return params;
    }

    private EditToken extractEditTokenFromJson(WikipediaApiResponse response) {
        return EditToken.of(
            response.getQuery().getTokens().getCsrftoken(),
            WikipediaTimestamp.of(
                response
                    .getQuery()
                    .getPages()
                    .stream()
                    .findFirst()
                    .orElseThrow()
                    .getRevisions()
                    .stream()
                    .findFirst()
                    .orElseThrow()
                    .getTimestamp()
            )
        );
    }
}
