package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
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

    @Loggable(prepend = true, value = Loggable.TRACE)
    @Override
    public List<WikipediaSection> getPageSections(int pageId, WikipediaLanguage lang) throws ReplacerException {
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
        return modelMapper.map(section, WikipediaSection.class);
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @Override
    public Optional<WikipediaPage> getPageByIdAndSection(int pageId, WikipediaSection section, WikipediaLanguage lang)
        throws ReplacerException {
        WikipediaApiResponse apiResponse = wikipediaRequestService.executeGetRequest(
            buildPageIdsAndSectionRequestParams(pageId, section.getIndex()),
            lang
        );
        List<WikipediaPage> pages = extractPagesFromJson(apiResponse);
        return pages
            .stream()
            .findAny()
            .map(p -> p.withLang(lang).withSection(section.getIndex()).withAnchor(section.getAnchor()));
    }

    private Map<String, String> buildPageIdsAndSectionRequestParams(int pageId, int section) {
        Map<String, String> params = buildPageIdsRequestParams(PARAM_PAGE_IDS, Integer.toString(pageId));
        params.put("rvsection", Integer.toString(section));
        return params;
    }

    @Override
    @Loggable(value = Loggable.DEBUG)
    public PageSearchResult getPageIdsByStringMatch(
        String text,
        boolean caseSensitive,
        int offset,
        int limit,
        WikipediaLanguage lang
    ) throws ReplacerException {
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

    @Loggable(value = Loggable.DEBUG, ignore = ReplacerException.class)
    @Override
    public void savePageContent(
        int pageId,
        String pageContent,
        @Nullable Integer section,
        String currentTimestamp,
        WikipediaLanguage lang,
        OAuth1AccessToken accessToken
    ) throws ReplacerException {
        EditToken editToken = getEditToken(pageId, lang, accessToken);
        // Pre-check of edit conflicts
        if (currentTimestamp.compareTo(editToken.getTimestamp()) <= 0) {
            LOGGER.warn(
                "Page edited at the same time: {} - {} - {} - {} - {}",
                pageId,
                currentTimestamp,
                editToken.getTimestamp(),
                lang,
                pageContent
            );
            throw new ReplacerException(
                "Esta página de Wikipedia ha sido editada por otra persona. Recargue para revisarla de nuevo."
            );
        }

        wikipediaRequestService.executeSignedPostRequest(
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
        params.put("bot", "true");
        params.put("minor", "true");
        params.put("token", editToken.getCsrfToken());
        params.put("starttimestamp", currentTimestamp); // Timestamp when the editing process began
        params.put("basetimestamp", editToken.getTimestamp()); // Timestamp of the base revision
        return params;
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @VisibleForTesting
    EditToken getEditToken(int pageId, WikipediaLanguage lang, OAuth1AccessToken accessToken) throws ReplacerException {
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
            response.getQuery().getPages().get(0).getRevisions().get(0).getTimestamp()
        );
    }
}
