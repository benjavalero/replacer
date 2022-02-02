package es.bvalero.replacer.page.review;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.PageReplacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.page.removeobsolete.RemoveObsoletePageService;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/** Template class to find a review. There are different implementations according to the different search options. */
@Slf4j
abstract class PageReviewFinder {

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private PageIndexService pageIndexService;

    @Autowired
    private RemoveObsoletePageService removeObsoletePageService;

    @Autowired
    private PageReviewSectionFinder pageReviewSectionFinder;

    // Maximum 500 as it is used as page size when searching in Wikipedia
    // For the sake of the tests, we implement it as a variable.
    @Getter(AccessLevel.PROTECTED)
    @Setter(onMethod_ = @TestOnly)
    private int cacheSize = 500;

    // Cache the found pages candidates to be reviewed
    // to find faster the next one after the user reviews one.
    // This map can grow a lot. We use Caffeine cache to clean periodically the old or obsolete lists.
    private final Cache<String, PageSearchResult> cachedPageIds = Caffeine
        .newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();

    // Transient variable in order to keep the offset during different iterations while finding a review
    // TODO: Concurrent custom reviews could use incorrectly the same offset value
    @Getter(AccessLevel.PROTECTED)
    private Integer offset;

    void incrementOffset(int increment) {
        this.offset = this.offset == null ? 0 : this.offset + increment;
    }

    /** Find a page/section review for the given search options (if any) */
    Optional<PageReview> findRandomPageReview(PageReviewOptions options) {
        // Restart offset
        this.offset = null;

        // STEP 1: Find a candidate page to review
        // We just need a page ID as the lang is already given in the options
        Optional<Integer> randomPageId = findPageIdToReview(options);

        // STEP 2: Build if possible a review for the candidate page
        while (randomPageId.isPresent()) {
            // It may happen the page must not be reviewed, e.g. no replacements are found
            // for the given search options in the current page content.
            // In such a case, we try to start again with a new candidate page.

            // We assume that, while getting the review, in case the page is not valid and review is eventually empty,
            // the page will be marked somehow in order not to be retrieved again.
            Optional<PageReview> review = getPageReview(randomPageId.get(), options);
            if (review.isPresent()) {
                return review;
            }

            randomPageId = findPageIdToReview(options);
        }

        // If we get here, there are no more pages to review for the given options.
        return Optional.empty();
    }

    ///// STEP 1 /////

    // As it is quite common to keep on reviewing pages for the same search options,
    // instead of finding candidates one by one, we find a list of them and cache them.

    private Optional<Integer> findPageIdToReview(PageReviewOptions options) {
        Optional<Integer> pageId;
        String key = buildCacheKey(options);
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

    private String buildCacheKey(PageReviewOptions options) {
        return options.toStringSearchType();
    }

    private boolean cacheContainsKey(String key) {
        PageSearchResult result = cachedPageIds.getIfPresent(key);
        return result != null && !result.isEmpty();
    }

    private Optional<Integer> popPageIdFromCache(String key) {
        PageSearchResult result = cachedPageIds.getIfPresent(key);
        assert result != null;
        return result.popPageId();
    }

    /** Find and cache the list of pages to review for the given options. Returns false in case of no pages to review. */
    @VisibleForTesting
    boolean loadCache(PageReviewOptions options) {
        String key = buildCacheKey(options);
        PageSearchResult result = cachedPageIds.getIfPresent(key);
        assert result == null || result.isEmpty(); // !cacheContainsKey

        if (result != null && result.isEmptyTotal() && stopWhenEmptyTotal()) {
            // In some cases, in particular for custom replacements found with Wikipedia search,
            // as we are not marking the non-reviewed pages in the database,
            // we don't want to find the pages to review in an infinite loop.
            // It will be done again once the related cache expires.
            return false;
        }

        // Reload the cached result list
        PageSearchResult searchResult = findPageIdsToReview(options);
        // For the moment we retrieve and cache all the pages
        // at risk of getting more edit conflicts
        if (!searchResult.isEmpty()) {
            assert searchResult.getPageIds().size() <= getCacheSize();
            searchResult.addCachedPages(wikipediaService.getPagesByIds(options.getLang(), searchResult.getPageIds()));
        }
        cachedPageIds.put(key, searchResult);
        return !searchResult.isEmpty();
    }

    boolean stopWhenEmptyTotal() {
        return false;
    }

    abstract PageSearchResult findPageIdsToReview(PageReviewOptions options);

    ///// STEP 2 /////

    /** This step can be called independently in case we already know the ID of the page to review */
    Optional<PageReview> getPageReview(int pageId, PageReviewOptions options) {
        // STEP 2.1: Load the page from Wikipedia
        Optional<WikipediaPage> wikipediaPage = getPageFromWikipedia(pageId, options);

        // STEP 2.2: Build the review for the page, or return an empty review in case the page doesn't exist.
        return wikipediaPage.flatMap(page -> buildPageReview(page, options));
    }

    private Optional<WikipediaPage> getPageFromWikipedia(int pageId, PageReviewOptions options) {
        WikipediaPageId wikipediaPageId = WikipediaPageId.of(options.getLang(), pageId);

        // Try to get first the page from the cache in particular for custom review
        Optional<WikipediaPage> page = getCachedPage(pageId, options);
        if (page.isEmpty()) {
            page = wikipediaService.getPageById(wikipediaPageId);
        }
        if (page.isPresent()) {
            return page;
        } else {
            LOGGER.warn("No page found in Wikipedia for {}", wikipediaPageId);
            removeObsoletePageService.removeObsoletePages(Collections.singleton(wikipediaPageId));
        }

        return Optional.empty();
    }

    private Optional<WikipediaPage> getCachedPage(int pageId, PageReviewOptions options) {
        String key = buildCacheKey(options);
        PageSearchResult result = cachedPageIds.getIfPresent(key);
        return result == null ? Optional.empty() : result.getCachedPage(pageId);
    }

    private Optional<PageReview> buildPageReview(WikipediaPage page, PageReviewOptions options) {
        // STEP 2.2.1: Find the replacements in the page
        Collection<PageReplacement> replacements = findReplacements(page, options);

        if (replacements.isEmpty()) {
            return Optional.empty();
        }

        // STEP 2.2.2: Build an initial review for the complete page with the found replacements
        Integer numPending = findTotalResultsFromCache(options).orElse(null);
        PageReview pageReview = PageReview.of(page, null, replacements, numPending);

        // STEP 2.2.3: Try to reduce the review size by returning just a section of the page
        return Optional.of(pageReviewSectionFinder.findPageReviewSection(pageReview).orElse(pageReview));
    }

    private List<PageReplacement> findReplacements(WikipediaPage page, PageReviewOptions options) {
        List<PageReplacement> replacements = new LinkedList<>(findAllReplacements(page, options));

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

    abstract Collection<PageReplacement> findAllReplacements(WikipediaPage page, PageReviewOptions options);

    PageIndexResult indexReplacements(WikipediaPage page) {
        LOGGER.trace("Update page replacements in database");
        return pageIndexService.indexPage(page);
    }

    private Optional<Integer> findTotalResultsFromCache(PageReviewOptions options) {
        String key = buildCacheKey(options);
        // If a review is requested directly it is possible the cache doesn't exist
        // In case of custom replacements the number of pending will include pages with false positives
        PageSearchResult result = cachedPageIds.getIfPresent(key);
        // Include the current one as pending
        return result != null ? Optional.of(result.getTotal() + 1) : Optional.empty();
    }
}
