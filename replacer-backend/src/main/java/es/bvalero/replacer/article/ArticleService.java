package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
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
        }

        LOGGER.info("END Find review for article");
        return review;
    }

    private Optional<WikipediaPage> getArticleFromWikipedia(int articleId) {
        try {
            Optional<WikipediaPage> page = wikipediaService.getPageById(articleId);
            if (page.isPresent()) {
                // Check if the article is processable
                if (page.get().isRedirectionPage()) {
                    LOGGER.warn(String.format("Found article is a redirection page: %s - %s",
                            articleId, page.get().getTitle()));
                } else {
                    return page;
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
        List<ArticleReplacement> articleReplacements = findArticleReplacements(
                article.getContent(), subtype, suggestion);
        LOGGER.info("Potential replacements found in text: {}", articleReplacements.size());

        if (ReplacementFinderService.CUSTOM_FINDER_TYPE.equals(type)) {
            if (articleReplacements.isEmpty()) {
                // We add the custom replacement to the database  as reviewed to skip it after the next search in the API
                Replacement customReplacement = new Replacement(article.getId(), ReplacementFinderService.CUSTOM_FINDER_TYPE, subtype, 0);
                articleIndexService.reviewReplacementAsSystem(customReplacement, false);
            }
        } else {
            // We take profit and we update the database with the just calculated replacements (also when empty)
            LOGGER.info("Update article replacements in database");
            articleIndexService.indexArticleReplacements(article, articleReplacements);

            // To build the review we are only interested in the replacements of the given type and subtype
            // We can run the filter even with an empty list
            if (StringUtils.isNotBlank(subtype)) {
                articleReplacements = filterReplacementsByTypeAndSubtype(articleReplacements, type, subtype);
                LOGGER.info("Final replacements found in text after filtering: {}", articleReplacements.size());
            }
        }

        if (articleReplacements.isEmpty()) {
            return Optional.empty();
        } else {
            return getSectionReview(article, articleReplacements);
        }
    }

    private List<ArticleReplacement> findArticleReplacements(
            String articleContent, @Nullable String subtype, @Nullable String suggestion) {
        // Find the replacements sorted (the first ones in the list are the last in the text)
        List<ArticleReplacement> articleReplacements;
        if (StringUtils.isBlank(suggestion)) {
            articleReplacements = replacementFinderService.findReplacements(articleContent);
        } else {
            // Custom replacement
            articleReplacements = replacementFinderService.findCustomReplacements(articleContent, subtype, suggestion);
        }
        // Return the replacements sorted as they appear in the text
        articleReplacements.sort(Collections.reverseOrder());
        return articleReplacements;
    }

    private List<ArticleReplacement> filterReplacementsByTypeAndSubtype(
            List<ArticleReplacement> replacements, @Nullable String type, @Nullable String subtype) {
        return replacements.stream()
                .filter(replacement -> replacement.getType().equals(type) && replacement.getSubtype().equals(subtype))
                .collect(Collectors.toList());
    }

    private Optional<ArticleReview> getSectionReview(WikipediaPage article, List<ArticleReplacement> articleReplacements) {
        // We try to reduce the review size by returning just a section of the page

        try {
            // Get the sections from the Wikipedia API (better than calculating them by ourselves)
            List<WikipediaSection> sections = new ArrayList<>(wikipediaService.getPageSections(article.getId()));

            // Find the smallest section containing all the replacements
            Optional<WikipediaSection> smallestSection =
                    getSmallestSectionContainingAllReplacements(sections, articleReplacements);

            // Retrieve the section from Wikipedia API. Better than calculating it by ourselves, just in case.
            if (smallestSection.isPresent()) {
                Optional<WikipediaPage> pageSection = wikipediaService.getPageByIdAndSection
                        (article.getId(), smallestSection.get().getIndex());
                if (pageSection.isPresent()) {
                    return Optional.of(buildArticleReview(pageSection.get(),
                            translateReplacementsByOffset(articleReplacements, smallestSection.get().getByteOffset())));
                }
            } else {
                return Optional.of(buildArticleReview(article, articleReplacements));
            }
        } catch (WikipediaException e) {
            LOGGER.error("Error getting section review", e);
        }

        return Optional.empty();
    }

    private Optional<WikipediaSection> getSmallestSectionContainingAllReplacements(
            List<WikipediaSection> sections, List<ArticleReplacement> replacements) {
        WikipediaSection smallest = null;
        for (int i = 0; i < sections.size(); i++) {
            WikipediaSection section = sections.get(i);
            int start = section.getByteOffset();
            Integer end = null;
            for (int j = i + 1; j < sections.size() && end == null; j++) {
                if (sections.get(j).getLevel() <= section.getLevel()) {
                    end = sections.get(j).getByteOffset() - 1;
                }
            }

            // Check if all replacements are contained in the current section
            if (areAllReplacementsContainedInInterval(replacements, start, end)) {
                smallest = section;
            }
        }
        return Optional.ofNullable(smallest);
    }

    private boolean areAllReplacementsContainedInInterval(List<ArticleReplacement> replacements,
                                                          Integer start, @Nullable Integer end) {
        return replacements.stream().allMatch(rep -> isReplacementContainedInInterval(rep, start, end));
    }

    private boolean isReplacementContainedInInterval(ArticleReplacement replacement,
                                                     Integer start, @Nullable Integer end) {
        if (replacement.getStart() >= start) {
            if (end == null) {
                return true;
            } else {
                return replacement.getEnd() <= end;
            }
        } else {
            return false;
        }
    }

    private List<ArticleReplacement> translateReplacementsByOffset(List<ArticleReplacement> articleReplacements, int offset) {
        return articleReplacements.stream().map(rep -> rep.withStart(rep.getStart() - offset)).collect(Collectors.toList());
    }

    private ArticleReview buildArticleReview(WikipediaPage article, List<ArticleReplacement> articleReplacements) {
        return ArticleReview.builder()
                .articleId(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .section(article.getSection())
                .currentTimestamp(article.getQueryTimestamp())
                .replacements(articleReplacements).build();
    }

    /* DUMP INDEX */

    public List<Replacement> findDatabaseReplacementByArticles(int minArticleId, int maxArticleId) {
        return replacementRepository.findByArticles(minArticleId, maxArticleId);
    }

    /* MISSPELLINGS */

    public void deleteReplacementsByTextIn(Collection<String> texts) {
        replacementRepository.deleteBySubtypeIn(new HashSet<>(texts));
    }

}
