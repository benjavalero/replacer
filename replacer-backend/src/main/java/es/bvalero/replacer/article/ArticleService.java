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

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private WikipediaService wikipediaService;

    // We use sets to compare easily in the unit tests
    private Collection<Replacement> toSaveInBatch = new HashSet<>();
    private Collection<Replacement> toDeleteInBatch = new HashSet<>();

    private void indexArticleReplacements(WikipediaPage article, Collection<ArticleReplacement> articleReplacements) {
        LOGGER.debug("Index replacements for article: {}", article.getId());
        indexReplacements(
                convertArticleReplacements(article, articleReplacements),
                replacementRepository.findByArticleId(article.getId()),
                false);
    }

    public void indexReplacements(Collection<Replacement> replacements, Collection<Replacement> dbReplacements,
                                  boolean indexInBatch) {
        LOGGER.debug("Index replacements in DB. New: {}. Old: {}", replacements.size(), dbReplacements.size());
        replacements.forEach(replacement -> {
            Optional<Replacement> existing = findSameReplacementInCollection(replacement, dbReplacements);
            if (existing.isPresent()) {
                handleExistingReplacement(replacement, existing.get(), indexInBatch);
                dbReplacements.remove(existing.get());
            } else {
                // New replacement
                saveReplacement(replacement, indexInBatch);
            }
        });

        // Remove the remaining replacements
        dbReplacements.stream().filter(Replacement::isToBeReviewed).forEach(rep -> deleteReplacement(rep, indexInBatch));
    }

    private Optional<Replacement> findSameReplacementInCollection(Replacement replacement,
                                                                  Collection<Replacement> replacements) {
        return replacements.stream().filter(rep -> rep.isSame(replacement)).findFirst();
    }

    private void handleExistingReplacement(Replacement newReplacement, Replacement dbReplacement, boolean indexInBatch) {
        if (dbReplacement.getLastUpdate().isBefore(newReplacement.getLastUpdate())) { // DB older than Dump
            saveReplacement(dbReplacement.withLastUpdate(newReplacement.getLastUpdate()), indexInBatch);
        }
    }

    public void flushReplacementsInBatch() {
        LOGGER.debug("Save and delete replacements in database. To save: {}. To delete: {}",
                toSaveInBatch.size(), toDeleteInBatch.size());
        replacementRepository.deleteInBatch(toDeleteInBatch);
        replacementRepository.saveAll(toSaveInBatch);

        // Using .clear() the unit tests don't pass don't know why
        toDeleteInBatch = new HashSet<>();
        toSaveInBatch = new HashSet<>();

        // Flush and clear to avoid memory leaks (we are performing millions of updates when indexing the dump)
        replacementRepository.flush();
        replacementRepository.clear(); // This clears all the EntityManager
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
        return findRandomArticleToReview(null);
    }

    Optional<Integer> findRandomArticleToReview(@Nullable String word) {
        LOGGER.info("Start finding random article to review. Filter by word: {}", word);
        Optional<Replacement> randomReplacement = findRandomReplacementNotReviewedInDb(word);
        return randomReplacement.map(Replacement::getArticleId);
    }

    Optional<ArticleReview> findArticleReviewById(int articleId) {
        return findArticleReviewById(articleId, null);
    }

    Optional<ArticleReview> findArticleReviewById(int articleId, @Nullable String word) {
        try {
            WikipediaPage article = findArticleById(articleId);
            List<ArticleReplacement> articleReplacements = findArticleReplacements(article.getContent());

            // Index the found replacements to add the new ones and remove the obsolete ones from last index in DB
            LOGGER.info("Update database with found replacements");
            indexArticleReplacements(article, articleReplacements);

            if (StringUtils.isNotBlank(word)) {
                articleReplacements = filterReplacementsByWord(word, articleReplacements);
            }

            // Build the article review if the replacements found are valid
            // Note the DB has just been updated in case the word doesn't exist in the found replacements
            if (!articleReplacements.isEmpty()) {
                LOGGER.info("Finish finding random article to review: {}", article.getTitle());
                return Optional.of(ArticleReview.builder()
                        .setArticleId(article.getId())
                        .setTitle(article.getTitle())
                        .setContent(article.getContent())
                        .setReplacements(articleReplacements)
                        .setLastUpdate(article.getTimestamp())
                        .setCurrentTimestamp(article.getQueryTimestamp())
                        .build());
            }
        } catch (InvalidArticleException e) {
            LOGGER.warn("Found article is not valid. Delete from database.", e);
            deleteArticle(articleId);
        } catch (WikipediaException e) {
            LOGGER.error("Error retrieving page from Wikipedia", e);
        }

        return Optional.empty();
    }

    private Optional<Replacement> findRandomReplacementNotReviewedInDb(@Nullable String word) {
        PageRequest pagination = PageRequest.of(0, 1);
        List<Replacement> randomReplacements = StringUtils.isBlank(word)
                ? replacementRepository.findRandomToReview(pagination)
                : replacementRepository.findRandomByWordToReview(word, pagination);
        return randomReplacements.isEmpty() ? Optional.empty() : Optional.of(randomReplacements.get(0));
    }

    private WikipediaPage findArticleById(int articleId) throws InvalidArticleException, WikipediaException {
        WikipediaPage page = wikipediaService.getPageById(articleId)
                .orElseThrow(() -> new InvalidArticleException(String.format("No article found with ID: %s", articleId)));

        // Check if the article is processable
        if (page.isRedirectionPage()) {
            throw new InvalidArticleException("Found article is a redirection page");
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
    boolean saveArticleChanges(int articleId, String text, String reviewer, String lastUpdate, String currentTimestamp,
                               OAuth1AccessToken accessToken) {
        try {
            // Upload new content to Wikipedia
            wikipediaService.savePageContent(articleId, text, lastUpdate, currentTimestamp, accessToken);

            // Mark article as reviewed in the database
            return markArticleAsReviewed(articleId, reviewer);
        } catch (WikipediaException e) {
            LOGGER.error("Error saving article: {}", articleId, e);
            return false;
        }
    }

    boolean markArticleAsReviewed(int articleId, String reviewer) {
        LOGGER.info("Mark article as reviewed: {}", articleId);
        replacementRepository.findByArticleId(articleId).stream()
                .filter(Replacement::isToBeReviewed)
                .forEach(rep -> reviewReplacement(rep, reviewer));
        return true;
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

    List<ReplacementCount> findMisspellingsGrouped() {
        return replacementRepository.findMisspellingsGrouped();
    }

}
