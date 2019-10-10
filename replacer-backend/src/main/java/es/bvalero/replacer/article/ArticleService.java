package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleService {

    private static final int CACHE_SIZE = 100;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ArticleStatsService articleStatsService;

    @Autowired
    private ArticleIndexService articleIndexService;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private SectionReviewService sectionReviewService;

    @Autowired
    private ModelMapper modelMapper;

    // Cache the found articles candidates to be reviewed
    // to find faster the next one after the user reviews one
    private Map<String, Set<Integer>> cachedArticleIdsByTypeAndSubtype = new HashMap<>();


    /* FIND RANDOM ARTICLES WITH REPLACEMENTS */

    Optional<ArticleReview> findRandomArticleToReview() {
        return findRandomArticleToReview(null, null);
    }

    Optional<ArticleReview> findRandomArticleToReview(String type, String subtype) {
        return findRandomArticleToReview(type, subtype, null);
    }

    Optional<ArticleReview> findRandomArticleToReviewWithCustomReplacement(String replacement, String suggestion) {
        return findRandomArticleToReview(ReplacementFinderService.CUSTOM_FINDER_TYPE, replacement, suggestion);
    }

    /**
     * Find a random article to be reviewed.
     *
     * @param type       The type of the replacement the article must include. Optional.
     * @param subtype    The subtype of the replacement the article must include. Optional.
     *                   If specified, the type must be specified too.
     * @param suggestion The suggestion in case of custom replacements, i. e. type CUSTOM_FINDER_TYPE.
     * @return The review of the found article, or empty if there is no such an article.
     */
    private Optional<ArticleReview> findRandomArticleToReview(
            @Nullable String type, @Nullable String subtype, @Nullable String suggestion) {
        LOGGER.info("START Find random article to review. Type: {} - {} - {}", type, subtype, suggestion);

        Optional<Integer> randomArticleId = findArticleIdToReview(type, subtype);
        while (randomArticleId.isPresent()) {
            // Try to obtain the review from the found article
            // If so, cache the review and return the article ID
            // If not, find a new random article ID
            Optional<ArticleReview> review = findArticleReview(randomArticleId.get(), type, subtype, suggestion);
            if (review.isPresent()) {
                LOGGER.info("END Find random article to review. Found article ID: {}", randomArticleId.get());
                return review;
            }

            randomArticleId = findArticleIdToReview(type, subtype);
        }

        // If we get here, there are no more articles to review in the database
        LOGGER.info("END Find random article to review. No article found.");
        return Optional.empty();
    }

    private Optional<Integer> findArticleIdToReview(@Nullable String type, @Nullable String subtype) {
        // First we try to get the random replacement from the cache
        String key = buildReplacementCacheKey(type, subtype);
        Optional<Integer> articleId = getArticleIdFromCache(key);
        if (!articleId.isPresent()) {
            // If it is not cached we try to find it in the database/Wikipedia and add the results to the cache
            List<Integer> articleIds = findArticleIdsToReview(type, subtype);

            // In case of custom replacement check that the replacement has not already been reviewed
            if (ReplacementFinderService.CUSTOM_FINDER_TYPE.equals(type)) {
                articleIds.removeIf(id -> replacementRepository.countByArticleIdAndTypeAndSubtypeAndReviewerNotNull(
                        id, ReplacementFinderService.CUSTOM_FINDER_TYPE, subtype) > 0);
            }

            // Return the first result and cache the rest
            articleId = getFirstResultAndCacheTheRest(articleIds, key);

            if (!articleId.isPresent()) {
                // If finally there are no results empty the cached count for the replacement
                // No need to check if there exists something cached
                articleStatsService.removeCachedReplacementCount(type, subtype);
            }
        }

        return articleId;
    }

    private String buildReplacementCacheKey(@Nullable String type, @Nullable String subtype) {
        return StringUtils.isNotBlank(subtype) ? String.format("%s-%s", type, subtype) : "";
    }

    private Optional<Integer> getArticleIdFromCache(String key) {
        if (cachedArticleIdsByTypeAndSubtype.containsKey(key)) {
            Set<Integer> randomArticleIds = cachedArticleIdsByTypeAndSubtype.get(key);
            Optional<Integer> randomArticleId = randomArticleIds.stream().findFirst();
            randomArticleId.ifPresent(id -> removeArticleFromCache(id, key));
            return randomArticleId;
        }
        return Optional.empty();
    }

    private List<Integer> findArticleIdsToReview(@Nullable String type, @Nullable String subtype) {
        if (ReplacementFinderService.CUSTOM_FINDER_TYPE.equals(type)) {
            try {
                return new ArrayList<>(wikipediaService.getPageIdsByStringMatch(subtype));
            } catch (WikipediaException e) {
                LOGGER.error("Error searching page IDs from Wikipedia", e);
                return Collections.emptyList();
            }
        } else {
            PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
            if (StringUtils.isNotBlank(subtype)) {
                return replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(type, subtype, pagination);
            } else {
                return replacementRepository.findRandomArticleIdsToReview(pagination);
            }
        }
    }

    private Optional<Integer> getFirstResultAndCacheTheRest(List<Integer> articleIds, String cacheKey) {
        Optional<Integer> randomArticleIdFromDb = articleIds.stream().findFirst();
        cachedArticleIdsByTypeAndSubtype.put(cacheKey, new HashSet<>(articleIds));
        randomArticleIdFromDb.ifPresent(id -> removeArticleFromCache(id, cacheKey));
        return randomArticleIdFromDb;
    }

    private void removeArticleFromCache(int articleId, String cacheKey) {
        // If no type is specified (empty key) then remove the article ID from all the lists
        if (StringUtils.isBlank(cacheKey)) {
            cachedArticleIdsByTypeAndSubtype.values().forEach(list -> list.remove(articleId));
        } else if (cachedArticleIdsByTypeAndSubtype.containsKey(cacheKey)) {
            cachedArticleIdsByTypeAndSubtype.get(cacheKey).remove(articleId);
        }
    }

    /**
     * Build a review for the given article.
     *
     * @return The review for the given article, or empty if no replacements are found in the article or the article is not valid.
     */
    Optional<ArticleReview> findArticleReview(
            int articleId, @Nullable String type, @Nullable String subtype, @Nullable String suggestion) {
        LOGGER.info("START Find review for article. ID: {} - Type: {} - {} - {}", articleId, type, subtype, suggestion);

        Optional<ArticleReview> review = Optional.empty();
        // Load article from Wikipedia
        Optional<WikipediaPage> article = getArticleFromWikipedia(articleId);
        if (article.isPresent()) {
            review = getArticleReview(article.get(), type, subtype, suggestion);
        } else if (ReplacementFinderService.CUSTOM_FINDER_TYPE.equals(type)) {
            // We add the custom replacement to the database  as reviewed to skip it after the next search in the API
            addCustomReviewedReplacement(articleId, subtype);
        }

        LOGGER.info("END Find review for article");
        return review;
    }

    private Optional<WikipediaPage> getArticleFromWikipedia(int articleId) {
        try {
            Optional<WikipediaPage> page = wikipediaService.getPageById(articleId);
            if (page.isPresent()) {
                // Check if the article is processable
                if (page.get().isProcessableByContent()) {
                    return page;
                } else {
                    LOGGER.warn(String.format("Found article is not processable by content: %s - %s",
                            articleId, page.get().getTitle()));
                }
            } else {
                LOGGER.warn(String.format("No article found. ID: %s", articleId));
            }

            articleIndexService.reviewArticleAsSystem(articleId);
        } catch (WikipediaException e) {
            LOGGER.error("Error finding page from Wikipedia", e);
        }

        return Optional.empty();
    }

    private Optional<ArticleReview> getArticleReview(WikipediaPage article, String type, String subtype, String suggestion) {
        // Find the replacements in the article
        List<Replacement> replacements = findArticleReplacements(
                article.getContent(), subtype, suggestion);
        LOGGER.info("Potential replacements found in text: {}", replacements.size());

        if (ReplacementFinderService.CUSTOM_FINDER_TYPE.equals(type)) {
            if (replacements.isEmpty()) {
                // We add the custom replacement to the database  as reviewed to skip it after the next search in the API
                addCustomReviewedReplacement(article.getId(), subtype);
            }
        } else {
            // We take profit and we update the database with the just calculated replacements (also when empty)
            LOGGER.info("Update article replacements in database");
            articleIndexService.indexArticleReplacements(article, replacements);

            // To build the review we are only interested in the replacements of the given type and subtype
            // We can run the filter even with an empty list
            if (StringUtils.isNotBlank(subtype)) {
                replacements = filterReplacementsByTypeAndSubtype(replacements, type, subtype);
                LOGGER.info("Final replacements found in text after filtering: {}", replacements.size());
            }
        }

        if (replacements.isEmpty()) {
            return Optional.empty();
        } else {
            ArticleReview articleReview = buildArticleReview(article, replacements);

            // We try to reduce the review size by returning just a section of the page
            Optional<ArticleReview> sectionReview = sectionReviewService.findSectionReview(articleReview);
            if (sectionReview.isPresent()) {
                return sectionReview;
            } else {
                return Optional.of(articleReview);
            }
        }
    }

    private List<Replacement> findArticleReplacements(
            String articleContent, @Nullable String subtype, @Nullable String suggestion) {
        // Find the replacements sorted (the first ones in the list are the last in the text)
        List<Replacement> replacements;
        if (StringUtils.isBlank(suggestion)) {
            replacements = replacementFinderService.findReplacements(articleContent);
        } else {
            // Custom replacement
            replacements = replacementFinderService.findCustomReplacements(articleContent, subtype, suggestion);
        }
        // Return the replacements sorted as they appear in the text
        replacements.sort(Collections.reverseOrder());
        return replacements;
    }

    private void addCustomReviewedReplacement(int articleId, String subtype) {
        ReplacementEntity customReplacement = new ReplacementEntity(articleId, ReplacementFinderService.CUSTOM_FINDER_TYPE, subtype, 0);
        articleIndexService.reviewReplacementAsSystem(customReplacement);
    }

    private List<Replacement> filterReplacementsByTypeAndSubtype(
            List<Replacement> replacements, @Nullable String type, @Nullable String subtype) {
        return replacements.stream()
                .filter(replacement -> replacement.getType().equals(type) && replacement.getSubtype().equals(subtype))
                .collect(Collectors.toList());
    }

    ArticleReview buildArticleReview(WikipediaPage article, List<Replacement> replacements) {
        ArticleReview review = modelMapper.map(article, ArticleReview.class);
        review.setReplacements(replacements.stream().map(this::convertToDto).collect(Collectors.toList()));
        return review;
    }

    private ArticleReplacement convertToDto(Replacement replacement) {
        return modelMapper.map(replacement, ArticleReplacement.class);
    }

    /* MISSPELLINGS */

    public void deleteReplacementsByTextIn(Collection<String> texts) {
        replacementRepository.deleteBySubtypeIn(new HashSet<>(texts));
    }

}
