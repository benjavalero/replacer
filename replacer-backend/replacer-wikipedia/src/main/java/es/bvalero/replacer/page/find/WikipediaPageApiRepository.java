package es.bvalero.replacer.page.find;

import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import es.bvalero.replacer.wikipedia.api.WikipediaApiVerb;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@SuppressWarnings("java:S1192")
@Slf4j
@Service
@Profile("!offline")
public class WikipediaPageApiRepository implements WikipediaPageRepository {

    // Dependency injection
    private final WikipediaApiHelper wikipediaApiHelper;

    WikipediaPageApiRepository(WikipediaApiHelper wikipediaApiHelper) {
        this.wikipediaApiHelper = wikipediaApiHelper;
    }

    @Override
    public Optional<WikipediaPage> findByTitle(WikipediaLanguage lang, String pageTitle) {
        try {
            return getPagesByTitle(lang, pageTitle);
        } catch (WikipediaException e) {
            LOGGER.error("Error finding page by title: {} - {}", lang, pageTitle, e);
            return Optional.empty();
        }
    }

    private Optional<WikipediaPage> getPagesByTitle(WikipediaLanguage lang, String pageTitle)
        throws WikipediaException {
        WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
            .verb(WikipediaApiVerb.GET)
            .lang(lang)
            .params(buildPageIdsRequestParams("titles", pageTitle))
            .build();
        WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
        // Return the only value that should be in the map
        return extractPagesFromJson(apiResponse, lang).stream().findAny();
    }

    @Override
    public Optional<WikipediaPage> findByKey(PageKey pageKey) {
        // Return the only value that should be in the map
        try {
            return findByKeys(List.of(pageKey)).stream().findAny();
        } catch (WikipediaException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<WikipediaPage> findByKeys(Collection<PageKey> pageKeys) throws WikipediaException {
        // We assume all requested pages share the same language
        List<WikipediaLanguage> keyLangs = pageKeys.stream().map(PageKey::getLang).distinct().toList();
        if (keyLangs.size() != 1) {
            throw new IllegalArgumentException("All pages have to share the same language: " + StringUtils.join(pageKeys, ", "));
        }
        WikipediaLanguage lang = keyLangs.get(0);

        List<WikipediaPage> pages = new ArrayList<>(pageKeys.size());
        // There is a maximum number of pages to request
        // We split the request in several sub-lists
        List<Integer> idList = pageKeys.stream().map(PageKey::getPageId).toList();
        int start = 0;
        while (start < idList.size()) {
            List<Integer> subList = idList.subList(start, start + Math.min(idList.size() - start, MAX_PAGES_REQUESTED));
            pages.addAll(getPagesByIds(lang, subList));
            start += subList.size();
        }
        return pages;
    }

    private Collection<WikipediaPage> getPagesByIds(WikipediaLanguage lang, List<Integer> pageIds)
        throws WikipediaException {
        if (pageIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String pagesValue = StringUtils.join(pageIds, '|');
            WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
                .verb(WikipediaApiVerb.GET)
                .lang(lang)
                .params(buildPageIdsRequestParams("pageids", pagesValue))
                .build();
            WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
            return extractPagesFromJson(apiResponse, lang);
        } catch (OutOfMemoryError e) {
            // Sometimes (though rarely) the retrieved pages are huge (e.g. annexes) and throw an out-of-memory error
            // Retry recursively with smaller chunks
            LOGGER.error("Out-of-memory when retrieving pages by ID: {}", StringUtils.join(pageIds, SPACE));
            int half = pageIds.size() / 2;
            Collection<WikipediaPage> result = getPagesByIds(lang, pageIds.subList(0, half));
            result.addAll(getPagesByIds(lang, pageIds.subList(half, pageIds.size())));
            return result;
        } catch (Exception e) {
            LOGGER.error("Error finding pages by ID: {}", StringUtils.join(pageIds, SPACE), e);
            throw new WikipediaException(e);
        }
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
        return WikipediaPage.builder()
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
        WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
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
        return WikipediaSection.builder()
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
            WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
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

        WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
            .verb(WikipediaApiVerb.GET)
            .lang(searchRequest.getLang())
            .params(buildPageIdsByStringMatchRequestParams(searchRequest))
            .build();
        WikipediaSearchResult result = WikipediaSearchResult.ofEmpty();
        try {
            WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
            result = extractPageIdsFromSearchJson(apiResponse);
        } catch (WikipediaException e) {
            if (e.getMessage().startsWith("cirrussearch-regex-syntax-error")) {
                LOGGER.error("Invalid search by regex: " + apiRequest.getParams().get("srsearch"));
            } else {
                LOGGER.error("Error finding pages by content", e);
            }
        }
        LOGGER.debug("END Find Page by Content: {} results", result.getTotal());
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
        String insource = caseSensitive
            ? String.format("insource:/%s/", ReplacerUtils.escapeRegexChars(text))
            : String.format("insource:\"%s\"", text);
        String quoted = String.format("\"%s\" %s", text, insource);
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

        return WikipediaSearchResult.builder()
            .total(response.getQuery().getSearchinfo().getTotalhits())
            .pageIds(pageIds)
            .build();
    }
}
