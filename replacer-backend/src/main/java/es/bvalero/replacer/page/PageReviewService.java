package es.bvalero.replacer.page;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.common.domain.WikipediaPageSection;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementSuggestion;
import es.bvalero.replacer.page.index.PageIndexer;
import es.bvalero.replacer.page.index.PageNotProcessableException;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
abstract class PageReviewService {

    // Maximum 500 as it is used as page size when searching in Wikipedia
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PACKAGE) // For testing
    private int cacheSize = 500;

    // Cache the found pages candidates to be reviewed
    // to find faster the next one after the user reviews one.
    // This map can grow a lot. We use Caffeine cache to clean periodically the old or obsolete lists.
    private final Cache<String, PageSearchResult> cachedPageIds = Caffeine
        .newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private PageIndexer pageIndexer;

    @Autowired
    private SectionReviewService sectionReviewService;

    Optional<PageReview> findRandomPageReview(PageReviewOptions options) {
        // Retrieve an ID of a potential page to be replaced
        Optional<Integer> randomPageId = findPageIdToReview(options);
        while (randomPageId.isPresent()) {
            // Try to obtain the review from the found page
            // If not, find a new random page ID
            // We assume that in the review building, in case the page is not valid and review is eventually empty,
            // the page will be marked somehow in order not to be retrieved again.
            Optional<PageReview> review = getPageReview(randomPageId.get(), options);
            if (review.isPresent()) {
                return review;
            }

            randomPageId = findPageIdToReview(options);
        }

        // If we get here, there are no more pages to review
        return Optional.empty();
    }

    ///// CACHE /////

    private Optional<Integer> findPageIdToReview(PageReviewOptions options) {
        // First we try to get the random replacement from the cache
        Optional<Integer> pageId;
        String key = buildReplacementCacheKey(options);
        if (cacheContainsKey(key)) {
            pageId = popPageIdFromCache(key);
        } else if (loadCache(options)) {
            pageId = popPageIdFromCache(key);
        } else {
            pageId = Optional.empty();
        }
        LOGGER.debug("Found page ID to review: {} => {}", options, pageId.orElse(null));
        return pageId;
    }

    abstract String buildReplacementCacheKey(PageReviewOptions options);

    private boolean cacheContainsKey(String key) {
        PageSearchResult result = cachedPageIds.getIfPresent(key);
        return result != null && !result.isEmpty();
    }

    private Optional<Integer> popPageIdFromCache(String key) {
        PageSearchResult result = cachedPageIds.getIfPresent(key);
        assert result != null;
        return result.popPageId();
    }

    @VisibleForTesting
    boolean loadCache(PageReviewOptions options) {
        // In case the cached result list is empty but also the total then quit
        // Else reload the cached result list
        String key = buildReplacementCacheKey(options);
        PageSearchResult result = cachedPageIds.getIfPresent(key);
        if (result != null && result.isEmptyTotal()) {
            String type = options.getType();
            String subtype = options.getSubtype();
            if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype)) {
                replacementService.reviewAsSystemBySubtype(options.getLang(), type, subtype);
            }
            return false;
        } else {
            PageSearchResult pageIds = findPageIdsToReview(options);
            cachedPageIds.put(key, pageIds);
            return !pageIds.isEmpty();
        }
    }

    abstract PageSearchResult findPageIdsToReview(PageReviewOptions options);

    Optional<PageReview> getPageReview(int pageId, PageReviewOptions options) {
        Optional<PageReview> review = Optional.empty();

        // Load page from Wikipedia
        Optional<WikipediaPage> page = getPageFromWikipedia(pageId, options);
        if (page.isPresent()) {
            review = buildPageReview(page.get(), options);
        }

        return review;
    }

    private Optional<WikipediaPage> getPageFromWikipedia(int pageId, PageReviewOptions options) {
        try {
            WikipediaPageId wikipediaPageId = WikipediaPageId.of(options.getLang(), pageId);
            Optional<WikipediaPage> page = wikipediaService.getPageById(wikipediaPageId);
            if (page.isPresent()) {
                return page;
            } else {
                LOGGER.warn("No page found in Wikipedia for {}", wikipediaPageId);
                pageIndexer.indexObsoletePage(wikipediaPageId);
            }
        } catch (ReplacerException e) {
            LOGGER.error("Error finding page in Wikipedia for {} - {}", options.getLang(), pageId, e);
        }

        return Optional.empty();
    }

    private Optional<PageReview> buildPageReview(WikipediaPage page, PageReviewOptions options) {
        // Find the replacements in the page
        List<Replacement> replacements = findReplacements(page, options);

        if (replacements.isEmpty()) {
            return Optional.empty();
        } else {
            PageReview pageReview = buildPageReview(page, replacements, options);

            // Try to reduce the review size by returning just a section of the page
            Optional<PageReview> sectionReview = sectionReviewService.findSectionReview(pageReview);
            if (sectionReview.isPresent()) {
                return sectionReview;
            } else {
                return Optional.of(pageReview);
            }
        }
    }

    private List<Replacement> findReplacements(WikipediaPage page, PageReviewOptions options) {
        List<Replacement> replacements = findAllReplacements(page, options);

        // Return the replacements sorted as they appear in the text
        // So there is no need to sort them in the frontend
        replacements.sort(Collections.reverseOrder());
        LOGGER.debug(
            "Found {} replacements in page {} - {} for options {}",
            replacements.size(),
            page.getId(),
            page.getTitle(),
            options
        );
        return replacements;
    }

    abstract List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options);

    protected FinderPage convertToFinderPage(WikipediaPage page) {
        return FinderPage.of(page.getId().getLang(), page.getContent(), page.getTitle());
    }

    void indexReplacements(WikipediaPage page, List<Replacement> replacements) throws PageNotProcessableException {
        LOGGER.trace("Update page replacements in database");
        pageIndexer.indexPageReplacements(page, replacements);
    }

    @VisibleForTesting
    PageReview buildPageReview(WikipediaPage page, List<Replacement> replacements, PageReviewOptions options) {
        return PageReview.of(
            WikipediaPageSection.of(page, null),
            replacements.stream().map(this::convert).collect(Collectors.toList()),
            convert(options)
        );
    }

    private long findTotalResultsFromCache(PageReviewOptions options) {
        String key = buildReplacementCacheKey(options);
        // If a review is requested directly it is possible the cache doesn't exist
        PageSearchResult result = cachedPageIds.getIfPresent(key);
        return result != null ? result.getTotal() : 0L;
    }

    private PageReplacement convert(Replacement replacement) {
        return PageReplacement.of(
            replacement.getStart(),
            replacement.getText(),
            replacement.getSuggestions().stream().map(this::convert).collect(Collectors.toList())
        );
    }

    private PageReplacementSuggestion convert(ReplacementSuggestion suggestion) {
        return PageReplacementSuggestion.of(suggestion.getText(), suggestion.getComment());
    }

    private PageReviewSearch convert(PageReviewOptions options) {
        long numPending = findTotalResultsFromCache(options) + 1; // Include the current one as pending
        if (options.getType() == null) {
            return PageReviewSearch.builder().numPending(numPending).build();
        } else {
            return PageReviewSearch
                .builder()
                .numPending(numPending)
                .type(options.getType())
                .subtype(options.getSubtype())
                .suggestion(options.getSuggestion())
                .cs(options.getCs())
                .build();
        }
    }

    abstract void reviewPageReplacements(int pageId, PageReviewOptions options, String reviewer);
}
