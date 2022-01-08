package es.bvalero.replacer.wikipedia.api;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import es.bvalero.replacer.wikipedia.*;
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
class WikipediaApiService implements WikipediaService {

    private static final int MAX_PAGES_REQUESTED = 50;
    private static final String PARAM_ACTION = "action";
    private static final String VALUE_QUERY = "query";
    private static final String PARAM_PAGE_ID = "pageid";
    private static final String PARAM_PAGE_IDS = "pageids";
    private static final int MAX_OFFSET_LIMIT = 10000;

    @Autowired
    private WikipediaApiRequestHelper wikipediaApiRequestHelper;

    @Override
    public WikipediaUser getAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken)
        throws WikipediaException {
        return convertUserInfo(lang, getLoggedUserInfo(lang, accessToken));
    }

    private WikipediaUser convertUserInfo(WikipediaLanguage lang, WikipediaApiResponse.UserInfo userInfo) {
        return WikipediaUser
            .builder()
            .lang(lang)
            .name(userInfo.getName())
            .groups(
                userInfo
                    .getGroups()
                    .stream()
                    .map(WikipediaUserGroup::valueOfLabel)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet())
            )
            .build();
    }

    @VisibleForTesting
    WikipediaApiResponse.UserInfo getLoggedUserInfo(WikipediaLanguage lang, AccessToken accessToken)
        throws WikipediaException {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.GET)
            .lang(lang)
            .params(buildUserInfoRequestParams())
            .accessToken(accessToken)
            .build();
        WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
        return extractUserInfoFromJson(apiResponse);
    }

    private Map<String, String> buildUserInfoRequestParams() {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("meta", "userinfo");
        params.put("uiprop", "groups");
        return params;
    }

    private WikipediaApiResponse.UserInfo extractUserInfoFromJson(WikipediaApiResponse response) {
        return response.getQuery().getUserinfo();
    }

    @Override
    public WikipediaUser getWikipediaUser(WikipediaLanguage lang, String username) throws WikipediaException {
        return convertUser(lang, getLoggedUser(lang, username));
    }

    private WikipediaUser convertUser(WikipediaLanguage lang, WikipediaApiResponse.User user) {
        return WikipediaUser
            .builder()
            .lang(lang)
            .name(user.getName())
            .groups(
                user
                    .getGroups()
                    .stream()
                    .map(WikipediaUserGroup::valueOfLabel)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet())
            )
            .build();
    }

    @VisibleForTesting
    WikipediaApiResponse.User getLoggedUser(WikipediaLanguage lang, String username) throws WikipediaException {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.GET)
            .lang(lang)
            .params(buildUserRequestParams(username))
            .build();
        WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
        return extractUserFromJson(apiResponse);
    }

    private Map<String, String> buildUserRequestParams(String username) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ACTION, VALUE_QUERY);
        params.put("list", "users");
        params.put("ususers", username);
        params.put("usprop", "groups");
        return params;
    }

    private WikipediaApiResponse.User extractUserFromJson(WikipediaApiResponse response) {
        return response.getQuery().getUsers().get(0);
    }

    @Override
    public Optional<WikipediaPage> getPageByTitle(WikipediaLanguage lang, String pageTitle) {
        try {
            // Return the only value that should be in the map
            return getPagesByIds("titles", pageTitle, lang).stream().findAny();
        } catch (WikipediaException e) {
            LOGGER.error("Error getting page by title: {} - {}", lang, pageTitle, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<WikipediaPage> getPageById(WikipediaPageId id) {
        try {
            // Return the only value that should be in the map
            return getPagesByIds(List.of(id.getPageId()), id.getLang()).stream().findAny();
        } catch (WikipediaException e) {
            LOGGER.error("Error getting page by ID: {}", id, e);
            return Optional.empty();
        }
    }

    @VisibleForTesting
    Collection<WikipediaPage> getPagesByIds(List<Integer> pageIds, WikipediaLanguage lang) throws WikipediaException {
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
        params.put("prop", "revisions");
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
            .map(page -> convert(page, lang, queryTimestamp))
            .collect(Collectors.toUnmodifiableList());
    }

    private WikipediaPage convert(WikipediaApiResponse.Page page, WikipediaLanguage lang, String queryTimestamp) {
        return WikipediaPage
            .builder()
            .id(WikipediaPageId.of(lang, page.getPageid()))
            .namespace(WikipediaNamespace.valueOf(page.getNs()))
            .title(page.getTitle())
            .content(page.getRevisions().get(0).getSlots().getMain().getContent())
            .lastUpdate(WikipediaDateUtils.parseWikipediaTimestamp(page.getRevisions().get(0).getTimestamp()))
            .queryTimestamp(WikipediaDateUtils.parseWikipediaTimestamp(queryTimestamp))
            .build();
    }

    @Override
    public Collection<WikipediaSection> getPageSections(WikipediaPageId id) throws WikipediaException {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.GET)
            .lang(id.getLang())
            .params(buildPageSectionsRequestParams(id.getPageId()))
            .build();
        WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
        return extractSectionsFromJson(apiResponse);
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

    @Override
    public Optional<WikipediaPage> getPageSection(WikipediaPageId id, WikipediaSection section) {
        try {
            WikipediaApiRequest apiRequest = WikipediaApiRequest
                .builder()
                .verb(WikipediaApiRequestVerb.GET)
                .lang(id.getLang())
                .params(buildPageIdsAndSectionRequestParams(id.getPageId(), section.getIndex()))
                .build();
            WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
            Collection<WikipediaPage> pages = extractPagesFromJson(apiResponse, id.getLang());
            return pages.stream().findAny();
        } catch (WikipediaException e) {
            LOGGER.error("Error getting page section: {} - {}", id, section, e);
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
    public WikipediaSearchResult searchByText(
        WikipediaLanguage lang,
        Collection<WikipediaNamespace> namespaces,
        String text,
        boolean caseSensitive,
        int offset,
        int limit
    ) throws WikipediaException {
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
        WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
        return extractPageIdsFromSearchJson(apiResponse);
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
        params.put("srnamespace", StringUtils.join(namespaceIds, "|"));
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
        String quoted = String.format("\"%s\"", text);
        if (caseSensitive) {
            // Case-sensitive search with a regex
            quoted = String.format("%s insource:/%s/", quoted, quoted);
        }
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
    public void savePageContent(
        WikipediaPageId id,
        @Nullable Integer section,
        String pageContent,
        LocalDateTime queryTimestamp,
        String editSummary,
        AccessToken accessToken
    ) throws WikipediaException {
        EditToken editToken = getEditToken(id, accessToken);
        // Pre-check of edit conflicts
        if (queryTimestamp.compareTo(editToken.getTimestamp()) <= 0) {
            String message = String.format(
                "Page edited at the same time: %s -%s - %s",
                id,
                queryTimestamp,
                queryTimestamp
            );
            throw new WikipediaConflictException(message);
        }

        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.POST)
            .lang(id.getLang())
            .params(
                buildSavePageContentRequestParams(
                    id.getPageId(),
                    pageContent,
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
    EditToken getEditToken(WikipediaPageId id, AccessToken accessToken) throws WikipediaException {
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
