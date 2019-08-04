package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
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

    static final String SYSTEM_REVIEWER = "system";
    private static final int CACHE_SIZE = 100;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ArticleStatsService articleStatsService;

    @Autowired
    private ArticleIndexService articleIndexService;

    // Cache the found articles candidates to be reviewed
    // to find faster the next one after the user reviews one
    private Map<String, Set<Integer>> cachedArticleIdsByTypeAndSubtype = new HashMap<>();

    /* FIND RANDOM ARTICLES WITH REPLACEMENTS */

    Optional<Integer> findRandomArticleToReview(@Nullable String type, @Nullable String subtype) {
        LOGGER.info("START Find random article to review. Type: {} - Subtype: {}", type, subtype);

        // First we get the replacements from database and we cache them
        String key = type != null && subtype != null ? type + "-" + subtype : "";
        if (!cachedArticleIdsByTypeAndSubtype.containsKey(key)) {
            // Find replacements by type and subtype from the database
            PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
            List<Replacement> randomReplacements = StringUtils.isBlank(key)
                    ? replacementRepository.findRandomToReview(pagination)
                    : replacementRepository.findRandomToReviewByTypeAndSubtype(type, subtype, pagination);

            // Cache the results
            cachedArticleIdsByTypeAndSubtype.put(key,
                    randomReplacements.stream().map(Replacement::getArticleId).collect(Collectors.toSet()));
        }

        Set<Integer> articleIds = cachedArticleIdsByTypeAndSubtype.get(key);
        Optional<Integer> articleId = articleIds.stream().findFirst();
        if (articleId.isPresent()) {
            // Remove the replacement from the cached list and others for the same article
            articleIds.remove(articleId.get());

            // If the set gets empty we remove it from the map
            if (articleIds.isEmpty()) {
                cachedArticleIdsByTypeAndSubtype.remove(key);
            }
        } else {
            cachedArticleIdsByTypeAndSubtype.remove(key);

            // Empty the cached count for the replacement
            articleStatsService.removeCachedReplacements(type, subtype);
        }

        LOGGER.info("END Find random article to review. Found article ID: {}", articleId.orElse(null));
        return articleId;
    }

    Optional<Integer> findRandomArticleToReviewByCustomReplacement(String subtype) {
        LOGGER.info("START Find random article to review by custom replacement: {}", subtype);

        // First we get the replacements from Wikipedia and we cache them
        String key = ReplacementFinderService.CUSTOM_FINDER_TYPE + "-" + subtype;
        if (!cachedArticleIdsByTypeAndSubtype.containsKey(key)) {
            // Find replacements by subtype in Wikipedia and cache the results
            try {
                // Check that the custom replacement has not already been reviewed for the articles
                cachedArticleIdsByTypeAndSubtype.put(key,
                        wikipediaService.getPageIdsByStringMatch(subtype).stream()
                                .filter(id -> replacementRepository.findByArticleIdAndTypeAndSubtypeAndReviewerNotNull(
                                        id, ReplacementFinderService.CUSTOM_FINDER_TYPE, subtype).isEmpty())
                                .collect(Collectors.toSet()));
            } catch (WikipediaException e) {
                LOGGER.error("Error searching page IDs from Wikipedia", e);
                return Optional.empty();
            }
        }

        Set<Integer> articleIds = cachedArticleIdsByTypeAndSubtype.get(key);
        Optional<Integer> articleId = articleIds.stream().findFirst();
        if (articleId.isPresent()) {
            // Remove the replacement from the cached list and others for the same article
            articleIds.remove(articleId.get());

            // If the set gets empty we remove it from the map
            if (articleIds.isEmpty()) {
                cachedArticleIdsByTypeAndSubtype.remove(key);
            }
        } else {
            cachedArticleIdsByTypeAndSubtype.remove(key);
        }

        LOGGER.info("END Find random article to review. Found article ID: {}", articleId.orElse(null));
        return articleId;
    }

    /* FIND AN ARTICLE REVIEW */

    Optional<ArticleReview> findArticleReviewById(int articleId, @Nullable String type, @Nullable String subtype) {
        LOGGER.info("START Find review for article. ID: {} - Type: {} - Subtype: {}", articleId, type, subtype);
        try {
            WikipediaPage article = findArticleById(articleId);
            List<ArticleReplacement> articleReplacements = findArticleReplacements(article.getContent());
            LOGGER.info("Potential replacements found in text: {}", articleReplacements.size());

            // Index the found replacements to add the new ones and remove the obsolete ones from last index in DB
            LOGGER.info("Update article replacements in database");
            articleIndexService.indexArticleReplacements(article, articleReplacements);

            if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype)) {
                articleReplacements = filterReplacementsByTypeAndSubtype(articleReplacements, type, subtype);
            }
            LOGGER.info("Final replacements found in text after filtering: {}", articleReplacements.size());

            // Build the article review if the replacements found are valid
            // Note the DB has just been updated in case the subtype doesn't exist in the found replacements
            if (!articleReplacements.isEmpty()) {
                ArticleReview review = new ArticleReview(
                        article.getId(),
                        article.getTitle(),
                        article.getContent(),
                        articleReplacements,
                        article.getQueryTimestamp());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("END Find review for article: {}", review);
                } else {
                    LOGGER.info("END Find review for article: {} - {}. Replacements to review: {}",
                            articleId, review.getTitle(), articleReplacements.size());
                }
                return Optional.of(review);
            }
        } catch (InvalidArticleException e) {
            LOGGER.warn("Found article is not valid. Delete from database.", e);
            deleteArticle(articleId);
        } catch (WikipediaException e) {
            LOGGER.error("Error retrieving page from Wikipedia", e);
            // Do nothing and retry
        }

        LOGGER.info("END Find review for article. No article found to review.");
        return Optional.empty();
    }

    private WikipediaPage findArticleById(int articleId) throws InvalidArticleException, WikipediaException {
        WikipediaPage page = wikipediaService.getPageById(articleId)
                .orElseThrow(() -> new InvalidArticleException(String.format("No article found with ID: %s", articleId)));

        // Check if the article is processable
        if (page.isRedirectionPage()) {
            throw new InvalidArticleException(
                    String.format("Found article is a redirection page: %s - %s", articleId, page.getTitle()));
        }

        return page;
    }

    private List<ArticleReplacement> findArticleReplacements(String articleContent) {
        // Find the replacements sorted (the first ones in the list are the last in the text)
        List<ArticleReplacement> articleReplacements = replacementFinderService.findReplacements(articleContent);
        articleReplacements.sort(Collections.reverseOrder());
        return articleReplacements;
    }

    private List<ArticleReplacement> filterReplacementsByTypeAndSubtype(
            List<ArticleReplacement> replacements, String type, String subtype) {
        return replacements.stream()
                .filter(replacement -> type.equals(replacement.getType()) && subtype.equals(replacement.getSubtype()))
                .collect(Collectors.toList());
    }

    Optional<ArticleReview> findArticleReviewByIdAndCustomReplacement(int articleId, String subtype, String suggestion) {
        LOGGER.info("START Find review for article by custom replacement. ID: {} - Subtype: {} - Suggestion: {}",
                articleId, subtype, suggestion);
        try {
            WikipediaPage article = findArticleById(articleId);
            List<ArticleReplacement> articleReplacements =
                    findCustomArticleReplacements(article.getContent(), subtype, suggestion);
            LOGGER.info("Potential replacements found in text: {}", articleReplacements.size());

            // Build the article review if the replacements found are valid
            if (articleReplacements.isEmpty()) {
                // We add the custom replacement to the database  as reviewed to skip it after the next search in the API
                markArticleAsReviewed(articleId, ReplacementFinderService.CUSTOM_FINDER_TYPE, subtype, SYSTEM_REVIEWER);
            } else {
                ArticleReview review = new ArticleReview(
                        article.getId(),
                        article.getTitle(),
                        article.getContent(),
                        articleReplacements,
                        article.getQueryTimestamp());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("END Find review for article: {}", review);
                } else {
                    LOGGER.info("END Find review for article: {} - {}. Replacements to review: {}",
                            articleId, review.getTitle(), articleReplacements.size());
                }
                return Optional.of(review);
            }
        } catch (InvalidArticleException e) {
            LOGGER.warn("Found article is not valid. Delete from database.", e);
            deleteArticle(articleId);
        } catch (WikipediaException e) {
            LOGGER.error("Error retrieving page from Wikipedia", e);
            // Do nothing and retry
        }

        LOGGER.info("END Find review for article. No article found to review.");
        return Optional.empty();
    }

    private List<ArticleReplacement> findCustomArticleReplacements(
            String articleContent, String replacement, String suggestion) {
        // Find the replacements sorted (the first ones in the list are the last in the text)
        List<ArticleReplacement> articleReplacements =
                replacementFinderService.findCustomReplacements(articleContent, replacement, suggestion);
        articleReplacements.sort(Collections.reverseOrder());
        return articleReplacements;
    }

    /* DUMP INDEX */

    public List<Replacement> findDatabaseReplacementByArticles(int minArticleId, int maxArticleId) {
        return replacementRepository.findByArticles(minArticleId, maxArticleId);
    }

    private void deleteArticle(int articleId) {
        deleteArticles(Collections.singleton(articleId));
    }

    public void deleteArticles(Set<Integer> articleIds) {
        articleIds.forEach(id -> markArticleAsReviewed(id, null, null, SYSTEM_REVIEWER));
    }

    /* MISSPELLINGS */

    public void deleteReplacementsByTextIn(Collection<String> texts) {
        replacementRepository.deleteBySubtypeIn(new HashSet<>(texts));
    }

    /* SAVE CHANGES */

    void saveArticleChanges(int articleId, String text, @Nullable String type, @Nullable String subtype, String reviewer,
                            String currentTimestamp, OAuth1AccessToken accessToken) throws WikipediaException {

        // Upload new content to Wikipedia
        wikipediaService.savePageContent(articleId, text, currentTimestamp, accessToken);

        // Mark article as reviewed in the database
        markArticleAsReviewed(articleId, type, subtype, reviewer);
    }

    void markArticleAsReviewed(int articleId, @Nullable String type, @Nullable String subtype, String reviewer) {
        LOGGER.info("START Mark article as reviewed. ID: {}", articleId);
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype)) {
            if (ReplacementFinderService.CUSTOM_FINDER_TYPE.equals(type)) {
                // In case of custom replacements they don't exist in the database to be reviewed
                articleIndexService.reviewReplacement(
                        new Replacement(articleId, type, subtype, 0),
                        reviewer, false);
            } else {
                List<Replacement> toReview = replacementRepository.findByArticleIdAndTypeAndSubtypeAndReviewerIsNull(
                        articleId, type, subtype);
                toReview.forEach(rep -> articleIndexService.reviewReplacement(rep, reviewer, false));

                // Decrease the cached count for the replacement
                articleStatsService.decreaseCachedReplacementsCount(type, subtype, toReview.size());
            }
        } else {
            replacementRepository.findByArticleIdAndReviewerIsNull(articleId)
                    .forEach(rep -> articleIndexService.reviewReplacement(rep, reviewer, false));
        }
        LOGGER.info("END Mark article as reviewed. ID: {}", articleId);
    }

}
