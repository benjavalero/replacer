package es.bvalero.replacer.article;

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
abstract class ArticleReviewService {
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

    Optional<ArticleReview> findRandomArticleReview(ArticleReviewOptions options) {
        LOGGER.debug("START Find random article review");

        // Retrieve an ID of a potential page to be replaced
        Optional<Integer> randomArticleId = findArticleIdToReview(options);
        while (randomArticleId.isPresent()) {
            // Try to obtain the review from the found article
            // If not, find a new random article ID
            // We assume that in the review building, in case the article is not valid and review is eventually empty,
            // the article will be marked somehow in order not to be retrieved again.
            Optional<ArticleReview> review = getArticleReview(randomArticleId.get(), options);
            if (review.isPresent()) {
                return review;
            }

            randomArticleId = findArticleIdToReview(options);
        }

        // If we get here, there are no more articles to review
        LOGGER.info("END Find random article review. No article found.");
        return Optional.empty();
    }

    private Optional<Integer> findArticleIdToReview(ArticleReviewOptions options) {
        LOGGER.info("START Find random article ID...");
        // First we try to get the random replacement from the cache
        Optional<Integer> articleId;
        String key = buildReplacementCacheKey(options);
        if (cacheContainsKey(key)) {
            articleId = popArticleIdFromCache(key);
        } else if (loadCache(options)) {
            articleId = popArticleIdFromCache(key);
        } else {
            articleId = Optional.empty();
        }

        LOGGER.info("END Found random article: {}", articleId.orElse(null));
        return articleId;
    }

    abstract String buildReplacementCacheKey(ArticleReviewOptions options);

    private boolean cacheContainsKey(String key) {
        return cachedPageIds.containsKey(key) && !cachedPageIds.get(key).isEmpty();
    }

    private Optional<Integer> popArticleIdFromCache(String key) {
        return cachedPageIds.get(key).popPageId();
    }

    boolean loadCache(ArticleReviewOptions options) {
        PageSearchResult pageIds = findPageIdsToReview(options);
        String key = buildReplacementCacheKey(options);
        cachedPageIds.put(key, pageIds);
        return !pageIds.isEmpty();
    }

    abstract PageSearchResult findPageIdsToReview(ArticleReviewOptions options);

    Optional<ArticleReview> getArticleReview(int articleId, ArticleReviewOptions options) {
        LOGGER.info("START Build review for article: {}", articleId);
        Optional<ArticleReview> review = Optional.empty();

        // Load article from Wikipedia
        Optional<WikipediaPage> article = getArticleFromWikipedia(articleId, options);
        if (article.isPresent()) {
            review = buildArticleReview(article.get(), options);
        }

        LOGGER.info("END Build review for article: {}", articleId);
        return review;
    }

    abstract List<String> getIgnorableTemplates();

    private Optional<WikipediaPage> getArticleFromWikipedia(int articleId, ArticleReviewOptions options) {
        LOGGER.info("START Find Wikipedia article: {}", articleId);
        try {
            Optional<WikipediaPage> page = wikipediaService.getPageById(articleId, options.getLang());
            if (page.isPresent()) {
                // Check if the article is processable
                if (page.get().isProcessable(getIgnorableTemplates())) {
                    LOGGER.info("END Found Wikipedia article: {} - {}", articleId, page.get().getTitle());
                    return page;
                } else {
                    LOGGER.warn(
                        String.format(
                            "Found article is not processable by content: %s - %s",
                            articleId,
                            page.get().getTitle()
                        )
                    );
                }
            } else {
                LOGGER.warn(String.format("No article found. ID: %s", articleId));
            }

            // We get here if the article is not found or not processable
            setArticleAsReviewed(articleId, options);
        } catch (ReplacerException e) {
            LOGGER.error("Error finding page from Wikipedia", e);
        }

        LOGGER.info("Found no Wikipedia article: {}", articleId);
        return Optional.empty();
    }

    void setArticleAsReviewed(int articleId, ArticleReviewOptions options) {
        replacementIndexService.reviewArticleReplacementsAsSystem(articleId, options.getLang());
    }

    private Optional<ArticleReview> buildArticleReview(WikipediaPage article, ArticleReviewOptions options) {
        // Find the replacements in the article
        List<Replacement> replacements = findReplacements(article, options);

        if (replacements.isEmpty()) {
            return Optional.empty();
        } else {
            ArticleReview articleReview = buildArticleReview(article, replacements, options);

            // Try to reduce the review size by returning just a section of the page
            Optional<ArticleReview> sectionReview = sectionReviewService.findSectionReview(articleReview);
            if (sectionReview.isPresent()) {
                return sectionReview;
            } else {
                return Optional.of(articleReview);
            }
        }
    }

    private List<Replacement> findReplacements(WikipediaPage article, ArticleReviewOptions options) {
        LOGGER.info("START Find replacements for article: {}", article.getId());
        List<Replacement> replacements = findAllReplacements(article, options);

        // Return the replacements sorted as they appear in the text
        replacements.sort(Collections.reverseOrder());
        LOGGER.info("END Found {} replacements for article: {}", replacements.size(), article.getId());
        return replacements;
    }

    abstract List<Replacement> findAllReplacements(WikipediaPage article, ArticleReviewOptions options);

    ArticleReview buildArticleReview(
        WikipediaPage article,
        List<Replacement> replacements,
        ArticleReviewOptions options
    ) {
        ArticleReview review = modelMapper.map(article, ArticleReview.class);
        review.setReplacements(replacements.stream().map(this::convertToDto).collect(Collectors.toList()));
        review.setNumPending(findTotalResultsFromCache(options) + 1); // Include the current one as pending
        return review;
    }

    private long findTotalResultsFromCache(ArticleReviewOptions options) {
        String key = buildReplacementCacheKey(options);
        return cachedPageIds.get(key).getTotal();
    }

    private ArticleReplacement convertToDto(Replacement replacement) {
        return modelMapper.map(replacement, ArticleReplacement.class);
    }
}
