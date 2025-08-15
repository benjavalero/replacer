package es.bvalero.replacer.page.find;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementType;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.PageSaveRepository;
import es.bvalero.replacer.page.index.PageIndexApi;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;

/** Template class to find a review. There are different implementations according to the different search options. */
@Slf4j
abstract class ReviewFinder implements ReviewFinderApi {

    // Dependency injection
    private final WikipediaPageRepository wikipediaPageRepository;
    private final PageIndexApi pageIndexApi;
    private final PageRepository pageRepository;
    private final PageSaveRepository pageSaveRepository;
    private final ReviewSectionFinder reviewSectionFinder;

    // Maximum 500 as it is used as page size when searching in Wikipedia
    // If too big it may produce out-of-memory issues with the cached page contents
    // For the sake of the tests, we implement it as a variable.
    @Getter(AccessLevel.PACKAGE)
    @Setter(onMethod_ = @TestOnly)
    private int cacheSize = WikipediaPageRepository.MAX_SEARCH_RESULTS;

    // Cache the found pages candidates to be reviewed
    // to find faster the next one after the user reviews one.
    // This map can grow a lot. We use Caffeine cache to clean periodically the old or obsolete lists.
    private final Cache<String, PageSearchResult> cachedPageIds = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();

    ReviewFinder(
        WikipediaPageRepository wikipediaPageRepository,
        PageIndexApi pageIndexApi,
        PageRepository pageRepository,
        PageSaveRepository pageSaveRepository,
        ReviewSectionFinder reviewSectionFinder
    ) {
        this.wikipediaPageRepository = wikipediaPageRepository;
        this.pageIndexApi = pageIndexApi;
        this.pageRepository = pageRepository;
        this.pageSaveRepository = pageSaveRepository;
        this.reviewSectionFinder = reviewSectionFinder;
    }

    /** Find a page/section review for the given search options (if any) */
    public Optional<Review> findRandomPageReview(ReviewOptions options) {
        // Restart offset
        findCachedResult(options).ifPresent(PageSearchResult::resetOffset);

        // STEP 1: Find a candidate page to review
        // We just need a page ID as the lang is already given in the options
        Optional<PageKey> randomPageKey = findPageKeyToReview(options);

        // STEP 2: Build if possible a review for the candidate page
        while (randomPageKey.isPresent()) {
            // It may happen the page must not be reviewed, e.g. no replacements are found
            // for the given search options in the current page content.
            // In such a case, we try to start again with a new candidate page.

            // We assume that, while getting the review, in case the page is not valid and review is eventually empty,
            // the page will be marked somehow in order not to be retrieved again.
            Optional<Review> review = findPageReview(randomPageKey.get(), options);
            if (review.isPresent()) {
                return review;
            }

            randomPageKey = findPageKeyToReview(options);
        }

        // If we get here, there are no more pages to review for the given options.
        removePageCounts(options);
        return Optional.empty();
    }

    ///// STEP 1 /////

    // As it is quite common to keep on reviewing pages for the same search options,
    // instead of finding candidates one by one, we find a list of them and cache them.

    private Optional<PageKey> findPageKeyToReview(ReviewOptions options) {
        Optional<PageKey> pageKey;
        String key = buildCacheKey(options);
        if (cacheContainsKey(key)) {
            if (cacheIsEmpty(key)) {
                // The key could exist in the cache but the list of pages could be empty.
                // If the list of pages is really empty, we remove the key from the cache.
                if (loadCache(options)) {
                    pageKey = popPageKeyFromCache(key);
                } else {
                    pageKey = Optional.empty();
                    removeKeyFromCache(key);
                }
            } else {
                pageKey = popPageKeyFromCache(key);
            }
        } else if (loadCache(options)) {
            pageKey = popPageKeyFromCache(key);
        } else {
            pageKey = Optional.empty();
        }
        LOGGER.debug("Found page ID to review: {} => {}", options, pageKey.orElse(null));
        return pageKey;
    }

    private String buildCacheKey(ReviewOptions options) {
        return options.toString();
    }

    private boolean cacheContainsKey(String key) {
        PageSearchResult result = this.cachedPageIds.getIfPresent(key);
        return result != null;
    }

    private boolean cacheIsEmpty(String key) {
        PageSearchResult result = this.cachedPageIds.getIfPresent(key);
        assert result != null;
        return result.isEmpty();
    }

    private Optional<PageKey> popPageKeyFromCache(String key) {
        PageSearchResult result = this.cachedPageIds.getIfPresent(key);
        assert result != null;
        return result.popPageKey();
    }

    private void removeKeyFromCache(String key) {
        this.cachedPageIds.invalidate(key);
    }

    Optional<PageSearchResult> findCachedResult(ReviewOptions options) {
        String key = buildCacheKey(options);
        return Optional.ofNullable(this.cachedPageIds.getIfPresent(key));
    }

    /** Find and cache the list of pages to review for the given options. Returns false in case of no pages to review. */
    @VisibleForTesting
    boolean loadCache(ReviewOptions options) {
        String key = buildCacheKey(options);
        PageSearchResult result = this.cachedPageIds.getIfPresent(key);
        assert result == null || result.isEmpty(); // !cacheContainsKey

        if (!reloadIfCacheIsEmpty() && result != null && result.isEmptyTotal()) {
            // In some cases, in particular for custom replacements found with Wikipedia search,
            // as we are not marking the non-reviewed pages in the database,
            // we don't want to find the pages to review in an infinite loop.
            // It will be done again once the related cache expires.
            return false;
        }

        // Reload the cached result list
        PageSearchResult searchResult = findPageIdsToReview(options);
        this.cachedPageIds.put(key, searchResult);
        return !searchResult.isEmpty();
    }

    boolean reloadIfCacheIsEmpty() {
        return true;
    }

    abstract PageSearchResult findPageIdsToReview(ReviewOptions options);

    void removePageCounts(ReviewOptions options) {
        // Do nothing by default
    }

    ///// STEP 2 /////

    /** This step can be called independently in case we already know the ID of the page to review */
    public Optional<Review> findPageReview(PageKey pageKey, ReviewOptions options) {
        try {
            // STEP 2.1: Load the page from Wikipedia
            Optional<WikipediaPage> wikipediaPage = findPageFromWikipedia(pageKey);

            // STEP 2.2: Build the review for the page, or return an empty review in case the page doesn't exist.
            return wikipediaPage.flatMap(page -> buildPageReview(page, options));
        } catch (Exception e) {
            LOGGER.error("Error finding page review: {}", pageKey, e);
            pageSaveRepository.removeByKey(Set.of(pageKey));
            return Optional.empty();
        }
    }

    private Optional<WikipediaPage> findPageFromWikipedia(PageKey pageKey) {
        Optional<WikipediaPage> page = wikipediaPageRepository.findByKey(pageKey);
        if (page.isEmpty()) {
            // Find the page title in the database to improve the warning
            String pageDbTitle = pageRepository.findByKey(pageKey).map(IndexedPage::getTitle).orElse(null);
            LOGGER.warn("No page found in Wikipedia: {} - {}", pageKey, pageDbTitle);
            pageSaveRepository.removeByKey(Set.of(pageKey));
        }

        return page;
    }

    private Optional<Review> buildPageReview(WikipediaPage page, ReviewOptions options) {
        // STEP 2.2.1: Find the replacements in the page
        Collection<Replacement> replacements = findReplacements(page, options);

        if (replacements.isEmpty()) {
            return Optional.empty();
        }

        // STEP 2.2.2: Build an initial review for the complete page with the found replacements
        Integer numPending = findTotalResultsFromCache(options).orElse(null);
        assert numPending == null || numPending > 0;
        Review review = Review.of(page, null, replacements, numPending);

        // STEP 2.2.3: Try to reduce the review size by returning just a section of the page
        return Optional.of(reviewSectionFinder.findPageReviewSection(review).orElse(review));
    }

    private Collection<Replacement> findReplacements(WikipediaPage page, ReviewOptions options) {
        // Calculate all the standard replacements
        // We take profit, and we update the database with the just calculated replacements (also when empty).
        // If the page has not been indexed (or is not indexable) the collection of replacements is empty
        // Note this collection has already discarded the replacements reviewed in the past
        Collection<Replacement> standardReplacements = pageIndexApi.indexPage(page).getReplacements();

        // Decorate the standard replacements with different actions depending on the type of review
        Collection<Replacement> decoratedReplacements = decorateReplacements(page, options, standardReplacements);

        // Discard the replacements only available for bots or admin (if applicable)
        Collection<Replacement> allowedReplacements = discardForbiddenReplacements(decoratedReplacements, options);

        // Return the replacements sorted as they appear in the text so there is no need to sort them in the frontend
        // We can assume the given replacement collection is already sorted, but we sort it just in case.
        List<Replacement> replacements = new ArrayList<>(allowedReplacements);
        Collections.sort(replacements);
        LOGGER.debug(
            "Found {} replacements in page {} - {} for options {}",
            replacements.size(),
            page.getPageKey(),
            page.getTitle(),
            options
        );
        return replacements;
    }

    /* Apply different actions to the standard replacements depending on the type of review */
    abstract Collection<Replacement> decorateReplacements(
        WikipediaPage page,
        ReviewOptions options,
        Collection<Replacement> replacements
    );

    Collection<Replacement> filterReplacementsByType(Collection<Replacement> replacements, ReplacementType type) {
        return replacements.stream().filter(replacement -> Objects.equals(replacement.getType(), type)).toList();
    }

    private Collection<Replacement> discardForbiddenReplacements(
        Collection<Replacement> replacements,
        ReviewOptions options
    ) {
        return replacements.stream().filter(r -> !isTypeForbidden(r.getType(), options.getUser())).toList();
    }

    private boolean isTypeForbidden(ReplacementType type, User user) {
        return type instanceof StandardType standardType && standardType.isTypeForbidden(user.isAdmin());
    }

    private Optional<Integer> findTotalResultsFromCache(ReviewOptions options) {
        String key = buildCacheKey(options);
        // If a review is requested directly it is possible the cache doesn't exist
        // In case of custom replacements the number of pending will include pages with false positives
        PageSearchResult result = this.cachedPageIds.getIfPresent(key);
        // Include the current one as pending
        return result != null ? Optional.of(result.getTotal() + 1) : Optional.empty();
    }
}
