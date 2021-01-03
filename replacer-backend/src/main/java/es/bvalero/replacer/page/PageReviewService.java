package es.bvalero.replacer.page;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.PageSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
abstract class PageReviewService {

    static final int CACHE_SIZE = 100; // Maximum 500 as it is used as page size when searching in Wikipedia
    // Cache the found pages candidates to be reviewed
    // to find faster the next one after the user reviews one
    private final Map<String, PageSearchResult> cachedPageIds = new HashMap<>();

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ReplacementDao replacementDao;

    @Autowired
    private SectionReviewService sectionReviewService;

    @Autowired
    private ModelMapper modelMapper;

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
        LOGGER.debug("Found page ID to review: {}", pageId);
        return pageId;
    }

    abstract String buildReplacementCacheKey(PageReviewOptions options);

    private boolean cacheContainsKey(String key) {
        return cachedPageIds.containsKey(key) && !cachedPageIds.get(key).isEmpty();
    }

    private Optional<Integer> popPageIdFromCache(String key) {
        return cachedPageIds.get(key).popPageId();
    }

    @VisibleForTesting
    boolean loadCache(PageReviewOptions options) {
        PageSearchResult pageIds = findPageIdsToReview(options);
        String key = buildReplacementCacheKey(options);
        cachedPageIds.put(key, pageIds);
        return !pageIds.isEmpty();
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

    abstract List<String> getIgnorableTemplates();

    private Optional<WikipediaPage> getPageFromWikipedia(int pageId, PageReviewOptions options) {
        try {
            Optional<WikipediaPage> page = wikipediaService.getPageById(pageId, options.getLang());
            if (page.isPresent()) {
                if (validatePage(page.get())) {
                    LOGGER.debug("Found Wikipedia page: {} - {}", page.get().getId(), page.get().getTitle());
                    return page;
                }
            } else {
                LOGGER.warn("No page found in Wikipedia: {}", pageId);
            }

            // We get here if the page is not found or not processable
            setPageAsReviewed(pageId, options);
        } catch (ReplacerException e) {
            LOGGER.error("Error finding page in Wikipedia", e);
        }

        return Optional.empty();
    }

    private boolean validatePage(WikipediaPage page) {
        try {
            page.validateProcessable(getIgnorableTemplates());
            return true;
        } catch (ReplacerException e) {
            LOGGER.warn("{} - {} - {}", e.getMessage(), page.getId(), page.getTitle());
            return false;
        }
    }

    void setPageAsReviewed(int pageId, PageReviewOptions options) {
        // These reviewed replacements will be cleaned up in the next dump indexation
        replacementDao.reviewByPageId(options.getLang(), pageId, null, null, ReplacementEntity.REVIEWER_SYSTEM);
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
        replacements.sort(Collections.reverseOrder());
        LOGGER.debug("Found page replacements for page {}: {}", page.getId(), replacements.size());
        return replacements;
    }

    abstract List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options);

    @VisibleForTesting
    PageReview buildPageReview(WikipediaPage page, List<Replacement> replacements, PageReviewOptions options) {
        PageReview review = modelMapper.map(page, PageReview.class);
        review.setReplacements(replacements.stream().map(this::convertToDto).collect(Collectors.toList()));
        review.setNumPending(findTotalResultsFromCache(options) + 1); // Include the current one as pending
        return review;
    }

    private long findTotalResultsFromCache(PageReviewOptions options) {
        String key = buildReplacementCacheKey(options);
        // If a review is requested directly it is possible the cache doesn't exist
        return cachedPageIds.containsKey(key) ? cachedPageIds.get(key).getTotal() : 0L;
    }

    private PageReplacement convertToDto(Replacement replacement) {
        return modelMapper.map(replacement, PageReplacement.class);
    }
}
