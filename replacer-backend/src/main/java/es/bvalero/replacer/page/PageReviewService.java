package es.bvalero.replacer.page;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.PageSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private SectionReviewService sectionReviewService;

    @Autowired
    private ModelMapper modelMapper;

    Optional<PageReview> findRandomPageReview(PageReviewOptions options) {
        LOGGER.debug("START Find random page review");

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
        LOGGER.info("END Find random page review. No page found.");
        return Optional.empty();
    }

    private Optional<Integer> findPageIdToReview(PageReviewOptions options) {
        LOGGER.info("START Find random page ID...");
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

        LOGGER.info("END Found random page: {}", pageId.orElse(null));
        return pageId;
    }

    abstract String buildReplacementCacheKey(PageReviewOptions options);

    private boolean cacheContainsKey(String key) {
        return cachedPageIds.containsKey(key) && !cachedPageIds.get(key).isEmpty();
    }

    private Optional<Integer> popPageIdFromCache(String key) {
        return cachedPageIds.get(key).popPageId();
    }

    boolean loadCache(PageReviewOptions options) {
        PageSearchResult pageIds = findPageIdsToReview(options);
        String key = buildReplacementCacheKey(options);
        cachedPageIds.put(key, pageIds);
        return !pageIds.isEmpty();
    }

    abstract PageSearchResult findPageIdsToReview(PageReviewOptions options);

    Optional<PageReview> getPageReview(int pageId, PageReviewOptions options) {
        LOGGER.info("START Build review for page: {}", pageId);
        Optional<PageReview> review = Optional.empty();

        // Load page from Wikipedia
        Optional<WikipediaPage> page = getPageFromWikipedia(pageId, options);
        if (page.isPresent()) {
            review = buildPageReview(page.get(), options);
        }

        LOGGER.info("END Build review for page: {}", pageId);
        return review;
    }

    abstract List<String> getIgnorableTemplates();

    private Optional<WikipediaPage> getPageFromWikipedia(int pageId, PageReviewOptions options) {
        LOGGER.info("START Find Wikipedia page: {}", pageId);
        try {
            Optional<WikipediaPage> page = wikipediaService.getPageById(pageId, options.getLang());
            if (page.isPresent()) {
                // Check if the page is processable
                if (page.get().isProcessable(getIgnorableTemplates())) {
                    LOGGER.info("END Found Wikipedia page: {} - {}", pageId, page.get().getTitle());
                    return page;
                } else {
                    LOGGER.warn(
                        String.format(
                            "Found page is not processable by content: %s - %s",
                            pageId,
                            page.get().getTitle()
                        )
                    );
                }
            } else {
                LOGGER.warn(String.format("No page found. ID: %s", pageId));
            }

            // We get here if the page is not found or not processable
            setPageAsReviewed(pageId, options);
        } catch (ReplacerException e) {
            LOGGER.error("Error finding page from Wikipedia", e);
        }

        LOGGER.info("Found no Wikipedia page: {}", pageId);
        return Optional.empty();
    }

    void setPageAsReviewed(int pageId, PageReviewOptions options) {
        // TODO: These reviewed replacements will be cleaned up in the next dump indexation
        replacementIndexService.reviewPageReplacementsAsSystem(pageId, options.getLang());
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
        LOGGER.info("START Find replacements for page: {}", page.getId());
        List<Replacement> replacements = findAllReplacements(page, options);

        // Return the replacements sorted as they appear in the text
        replacements.sort(Collections.reverseOrder());
        LOGGER.info("END Found {} replacements for page: {}", replacements.size(), page.getId());
        return replacements;
    }

    abstract List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options);

    PageReview buildPageReview(WikipediaPage page, List<Replacement> replacements, PageReviewOptions options) {
        PageReview review = modelMapper.map(page, PageReview.class);
        review.setReplacements(replacements.stream().map(this::convertToDto).collect(Collectors.toList()));
        review.setNumPending(findTotalResultsFromCache(options) + 1); // Include the current one as pending
        return review;
    }

    private long findTotalResultsFromCache(PageReviewOptions options) {
        String key = buildReplacementCacheKey(options);
        return cachedPageIds.get(key).getTotal();
    }

    private PageReplacement convertToDto(Replacement replacement) {
        return modelMapper.map(replacement, PageReplacement.class);
    }
}
