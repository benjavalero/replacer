package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides methods to find articles with potential replacements.
 */
@Service
public class ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);
    private static final int BATCH_SIZE = 1000;
    private static final int CACHE_SIZE = 100;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private WikipediaService wikipediaService;

    // We use sets to compare easily in the unit tests
    private Collection<Replacement> toSaveInBatch = new HashSet<>();
    private Collection<Replacement> toDeleteInBatch = new HashSet<>();

    private Map<String, Set<Integer>> cachedArticleIdsByWord = new HashMap<>();
    private List<ReplacementCount> cachedReplacementCount = new ArrayList<>();

    private void indexArticleReplacements(WikipediaPage article, Collection<ArticleReplacement> articleReplacements) {
        LOGGER.debug("START Index replacements for article: {} - {}", article.getId(), article.getTitle());
        indexReplacements(
                convertArticleReplacements(article, articleReplacements),
                replacementRepository.findByArticleId(article.getId()),
                false);
        LOGGER.debug("END Index replacements for article: {} - {}", article.getId(), article.getTitle());
    }

    public void indexReplacements(Collection<Replacement> replacements, Collection<Replacement> dbReplacements,
                                  boolean indexInBatch) {
        LOGGER.debug("START Index list of replacements\n" +
                        "New: {} - {}\n" +
                        "Old: {} - {}",
                replacements.size(), replacements,
                dbReplacements.size(), dbReplacements);
        replacements.forEach(replacement -> {
            Optional<Replacement> existing = findSameReplacementInCollection(replacement, dbReplacements);
            if (existing.isPresent()) {
                handleExistingReplacement(replacement, existing.get(), indexInBatch);
                dbReplacements.remove(existing.get());
            } else {
                // New replacement
                saveReplacement(replacement, indexInBatch);
                LOGGER.debug("Replacement inserted in DB: {}", replacement);
            }
        });

        // Remove the remaining replacements
        dbReplacements.stream().filter(Replacement::isToBeReviewed).forEach(rep -> {
            deleteReplacement(rep, indexInBatch);
            LOGGER.debug("Replacement deleted in DB: {}", rep);
        });
        LOGGER.debug("END Index list of replacements");
    }

    private Optional<Replacement> findSameReplacementInCollection(Replacement replacement,
                                                                  Collection<Replacement> replacements) {
        return replacements.stream().filter(rep -> rep.isSame(replacement)).findFirst();
    }

    private void handleExistingReplacement(Replacement newReplacement, Replacement dbReplacement, boolean indexInBatch) {
        if (dbReplacement.getLastUpdate().isBefore(newReplacement.getLastUpdate())) { // DB older than Dump
            Replacement updated = dbReplacement.withLastUpdate(newReplacement.getLastUpdate());
            saveReplacement(updated, indexInBatch);
            LOGGER.debug("Replacement updated in DB: {}", updated);
        } else {
            LOGGER.debug("Replacement existing in DB: {}", dbReplacement);
        }
    }

    public void flushReplacementsInBatch() {
        LOGGER.debug("START Save and delete replacements in database. To save: {}. To delete: {}",
                toSaveInBatch.size(), toDeleteInBatch.size());
        replacementRepository.deleteInBatch(toDeleteInBatch);
        replacementRepository.saveAll(toSaveInBatch);

        // Using .clear() the unit tests don't pass don't know why
        toDeleteInBatch = new HashSet<>();
        toSaveInBatch = new HashSet<>();

        // Flush and clear to avoid memory leaks (we are performing millions of updates when indexing the dump)
        replacementRepository.flush();
        replacementRepository.clear(); // This clears all the EntityManager
        LOGGER.debug("END Save and delete replacements in database");
    }

    private void saveReplacement(Replacement replacement, boolean saveInBatch) {
        if (saveInBatch) {
            toSaveInBatch.add(replacement);
            if (toSaveInBatch.size() >= BATCH_SIZE) {
                flushReplacementsInBatch();
            }
        } else {
            replacementRepository.save(replacement);
        }
    }

    private void deleteReplacement(Replacement replacement, boolean deleteInBatch) {
        if (deleteInBatch) {
            toDeleteInBatch.add(replacement);
            if (toDeleteInBatch.size() >= BATCH_SIZE) {
                flushReplacementsInBatch();
            }
        } else {
            replacementRepository.delete(replacement);
        }
    }

    public Collection<Replacement> convertArticleReplacements(WikipediaPage article,
                                                              Collection<ArticleReplacement> articleReplacements) {
        return articleReplacements.stream().map(
                articleReplacement -> new Replacement(
                        article.getId(), articleReplacement.getType(), articleReplacement.getSubtype(),
                        articleReplacement.getStart())
                        .withLastUpdate(article.getLastUpdate().toLocalDate()))
                .collect(Collectors.toList());
    }

    Optional<Integer> findRandomArticleToReview() {
        return findRandomArticleToReview("");
    }

    Optional<Integer> findRandomArticleToReview(String word) {
        LOGGER.info("START Find random article to review. Word: {}", word);

        // First we get the replacements from database and we cache them
        if (!cachedArticleIdsByWord.containsKey(word)) {
            // Find replacements by word from the database
            PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
            List<Replacement> randomReplacements = StringUtils.isBlank(word)
                    ? replacementRepository.findRandomToReview(pagination)
                    : replacementRepository.findRandomByWordToReview(word, pagination);

            // Cache the results
            Set<Integer> articleIds = randomReplacements.stream()
                    .map(Replacement::getArticleId).collect(Collectors.toSet());
            cachedArticleIdsByWord.put(word, articleIds);
        }

        Set<Integer> articleIdsByWord = cachedArticleIdsByWord.get(word);
        Optional<Integer> articleId = articleIdsByWord.stream().findFirst();
        if (articleId.isPresent()) {
            // Remove the replacement from the cached list and others for the same article
            articleIdsByWord.remove(articleId.get());

            // If the set gets empty we remove it from the map
            if (articleIdsByWord.isEmpty()) {
                cachedArticleIdsByWord.remove(word);
            }
        } else {
            cachedArticleIdsByWord.remove(word);
        }

        LOGGER.info("END Find random article to review. Found article ID: {}", articleId.orElse(null));
        return articleId;
    }

    Optional<ArticleReview> findArticleReviewById(int articleId) {
        return findArticleReviewById(articleId, null);
    }

    Optional<ArticleReview> findArticleReviewById(int articleId, @Nullable String word) {
        LOGGER.info("START Find review for article. ID: {} - Word: {}", articleId, word);
        try {
            WikipediaPage article = findArticleById(articleId);
            List<ArticleReplacement> articleReplacements = findArticleReplacements(article.getContent());
            LOGGER.info("Potential replacements found in text: {}", articleReplacements.size());

            // Index the found replacements to add the new ones and remove the obsolete ones from last index in DB
            LOGGER.info("Update article replacements in database");
            indexArticleReplacements(article, articleReplacements);

            if (StringUtils.isNotBlank(word)) {
                articleReplacements = filterReplacementsByWord(word, articleReplacements);
            }
            LOGGER.info("Final replacements found in text after filtering: {}", articleReplacements.size());

            // Build the article review if the replacements found are valid
            // Note the DB has just been updated in case the word doesn't exist in the found replacements
            if (!articleReplacements.isEmpty()) {
                ArticleReview review = ArticleReview.builder()
                        .setArticleId(article.getId())
                        .setTitle(article.getTitle())
                        .setContent(article.getContent())
                        .setReplacements(articleReplacements)
                        .setLastUpdate(article.getTimestamp())
                        .setCurrentTimestamp(article.getQueryTimestamp())
                        .build();
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

    public void deleteArticles(Set<Integer> articleIds) {
        replacementRepository.deleteByArticleIdIn(articleIds);
    }

    private void deleteArticle(int articleId) {
        replacementRepository.deleteByArticleId(articleId);
    }

    public List<Replacement> findDatabaseReplacementByArticles(int minArticleId, int maxArticleId) {
        return replacementRepository.findByArticles(minArticleId, maxArticleId);
    }

    private List<ArticleReplacement> filterReplacementsByWord(String word, List<ArticleReplacement> replacements) {
        return replacements.stream()
                .filter(replacement -> word.equals(replacement.getSubtype()))
                .collect(Collectors.toList());
    }

    /**
     * Saves in Wikipedia the changes on an article validated in the front-end.
     */
    void saveArticleChanges(int articleId, String text, String reviewer, String lastUpdate, String currentTimestamp,
                            OAuth1AccessToken accessToken) throws WikipediaException {

        // Upload new content to Wikipedia
        wikipediaService.savePageContent(articleId, text, lastUpdate, currentTimestamp, accessToken);

        // Mark article as reviewed in the database
        markArticleAsReviewed(articleId, reviewer);
    }

    void markArticleAsReviewed(int articleId, String reviewer) {
        LOGGER.info("START Mark article as reviewed. ID: {}", articleId);
        replacementRepository.findByArticleId(articleId).stream()
                .filter(Replacement::isToBeReviewed)
                .forEach(rep -> reviewReplacement(rep, reviewer));
        LOGGER.info("END Mark article as reviewed. ID: {}", articleId);
    }

    private void reviewReplacement(Replacement replacement, String reviewer) {
        replacementRepository.save(replacement
                .withReviewer(reviewer)
                .withLastUpdate(LocalDate.now()));
    }

    long countReplacements() {
        return replacementRepository.count();
    }

    long countReplacementsReviewed() {
        return replacementRepository.countByReviewerIsNotNull();
    }

    long countReplacementsToReview() {
        return replacementRepository.countByReviewerIsNull();
    }

    /**
     * Update every 5 minutes the count of misspellings from Wikipedia
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void updateReplacementCount() {
        LOGGER.info("EXECUTE Scheduled update of grouped replacements count");
        LOGGER.info("START Count grouped replacements");
        List<ReplacementCount> count = replacementRepository.findMisspellingsGrouped();
        LOGGER.info("END Count grouped replacements. Size: {}", count.size());

        this.cachedReplacementCount.clear();
        this.cachedReplacementCount.addAll(count);
    }

    List<ReplacementCount> findMisspellingsGrouped() {
        return this.cachedReplacementCount;
    }

    public void deleteReplacementsByTextIn(Collection<String> texts) {
        replacementRepository.deleteBySubtypeIn(new HashSet<>(texts));
    }

}
