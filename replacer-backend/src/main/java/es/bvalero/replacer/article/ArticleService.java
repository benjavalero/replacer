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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides methods to find articles with potential replacements.
 */
@Service
public class ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);
    private static final String DEFAULT_REVIEWER = "system";

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private WikipediaService wikipediaService;

    @Value("${replacer.hide.empty.paragraphs}")
    private boolean trimText;

    // We use sets to compare easily in the unit tests
    private Collection<Replacement> toSave = new HashSet<>();
    private Collection<Replacement> toDelete = new HashSet<>();

    public void indexReplacements(Collection<Replacement> replacements) {
        LOGGER.debug("Index replacements: {}", replacements);
        Map<Integer, Collection<Replacement>> replacementsByArticle = buildReplacementMapByArticle(replacements);

        List<Replacement> dbReplacements = replacementRepository.findByArticleIdIn(replacementsByArticle.keySet());
        Map<Integer, Collection<Replacement>> dbReplacementsByArticle = buildReplacementMapByArticle(dbReplacements);

        replacementsByArticle.forEach((key, value) ->
                indexArticleReplacements(value, dbReplacementsByArticle.getOrDefault(key, Collections.emptySet())));

        flushReplacements();
    }

    public Map<Integer, Collection<Replacement>> buildReplacementMapByArticle(Collection<Replacement> replacements) {
        Map<Integer, Collection<Replacement>> map = new TreeMap<>();
        for (Replacement replacement : replacements) {
            int articleId = replacement.getArticleId();
            if (!map.containsKey(articleId)) {
                map.put(articleId, new HashSet<>());
            }
            map.get(articleId).add(replacement);
        }
        return map;
    }

    private void indexArticleReplacements(Collection<Replacement> replacements, Collection<Replacement> dbReplacements) {
        LOGGER.debug("Index replacements. New: {}. Old: {}", replacements, dbReplacements);
        for (Replacement replacement : replacements) {
            // Find if the replacement already exists in the database
            Optional<Replacement> existing = findSameReplacementInCollection(replacement, dbReplacements);
            if (existing.isPresent()) {
                handleExistingReplacement(replacement, existing.get());
                dbReplacements.remove(existing.get());
            } else {
                // Find if the replacement already exists in the database but migrated
                Optional<Replacement> migrated = findSameReplacementInCollection(replacement.migrate(), dbReplacements);
                if (migrated.isPresent()) {
                    handleExistingMigratedReplacement(replacement, migrated.get());
                } else {
                    // New replacement
                    addReplacement(replacement);
                }
            }
        }

        // Remove the remaining replacements
        for (Replacement dbReplacement : dbReplacements) {
            if (dbReplacement.isToBeReviewed()
                    || (dbReplacement.isReviewed() && dbReplacement.isMigrated())) {
                deleteReplacement(dbReplacement);
            }
        }
    }

    private Optional<Replacement> findSameReplacementInCollection(Replacement replacement, Collection<Replacement> replacements) {
        return replacements.stream().filter(rep -> rep.isSame(replacement)).findFirst();
    }

    private void handleExistingReplacement(Replacement newReplacement, Replacement dbReplacement) {
        if (dbReplacement.getLastUpdate().isBefore(newReplacement.getLastUpdate())
                && (dbReplacement.isToBeReviewed() || dbReplacement.isFixed())) {
            updateReplacementDate(dbReplacement, newReplacement.getLastUpdate());
        }
    }

    private void handleExistingMigratedReplacement(Replacement newReplacement, Replacement dbMigrated) {
        if (dbMigrated.isToBeReviewed()) {
            addReplacement(newReplacement);
        } else if (dbMigrated.isReviewed()) {
            addReplacement(newReplacement.withStatus(ReplacementStatus.REVIEWED));
        }
    }

    private void flushReplacements() {
        replacementRepository.deleteInBatch(toDelete);
        replacementRepository.saveAll(toSave);

        // Using .clear() the unit tests don't pass don't know why
        toDelete = new HashSet<>();
        toSave = new HashSet<>();

        // Flush and clear to avoid memory leaks (we are performing millions of updates when indexing the dump)
        replacementRepository.flush();
        replacementRepository.clear(); // This clears all the EntityManager
    }

    private void addReplacement(Replacement replacement) {
        toSave.add(replacement);
    }

    private void deleteReplacement(Replacement replacement) {
        toDelete.add(replacement);
    }

    private void updateReplacementDate(Replacement replacement, LocalDate lastUpdate) {
        toSave.add(replacement.withLastUpdate(lastUpdate));
    }

    public Collection<Replacement> convertArticleReplacements(WikipediaPage article, Collection<ArticleReplacement> articleReplacements) {
        return articleReplacements.stream().map(
                articleReplacement -> new Replacement(
                        article.getId(), articleReplacement.getType(), articleReplacement.getSubtype(), articleReplacement.getStart())
                        .withLastUpdate(article.getTimestamp()))
                .collect(Collectors.toList());
    }

    ArticleReview findRandomArticleToReview() throws UnfoundArticleException {
        return findRandomArticleToReview(null);
    }

    ArticleReview findRandomArticleToReview(@Nullable String word) throws UnfoundArticleException {
        LOGGER.info("Find random article to review. Filter by word: {}", word);
        ArticleReview review = null;
        while (review == null) {
            LOGGER.info("Find random replacement to review");
            Replacement randomReplacement = findRandomReplacementNotReviewedInDb(word)
                    .orElseThrow(() -> new UnfoundArticleException("No replacement found to be reviewed"));
            LOGGER.info("Found random replacement to review: {}", randomReplacement);

            try {
                LOGGER.info("Retrieve page related to the replacement with ID: {}", randomReplacement.getArticleId());
                WikipediaPage article = findArticleById(randomReplacement.getArticleId());

                LOGGER.info("Find replacements in article");
                List<ArticleReplacement> articleReplacements = findArticleReplacements(article.getContent());
                LOGGER.info("Found replacements in article: {}", articleReplacements.size());

                // Index the found replacements to add the new ones and remove the obsolete ones from last index in DB
                LOGGER.info("Update database with found replacements");
                indexReplacements(convertArticleReplacements(article, articleReplacements));

                // Build the article review if the replacements found are valid
                // Note the DB has just been updated in case the word doesn't exist in the found replacements
                if (!articleReplacements.isEmpty()
                        && checkWordExistsInReplacements(word, articleReplacements)) {
                    review = ArticleReview.builder()
                            .setArticleId(article.getId())
                            .setTitle(article.getTitle())
                            .setContent(article.getContent())
                            .setReplacements(articleReplacements)
                            .setTrimText(trimText)
                            .build();
                }
            } catch (InvalidArticleException e) {
                LOGGER.warn("Found article is not valid. Delete from database.", e);
                deleteArticle(randomReplacement.getArticleId());
            } catch (WikipediaException e) {
                LOGGER.error("Error retrieving page from Wikipedia", e);
            }
        }

        LOGGER.info("Return article to review: {}", review.getTitle());
        return review;
    }

    private Optional<Replacement> findRandomReplacementNotReviewedInDb(@Nullable String word) {
        PageRequest pagination = PageRequest.of(0, 1);
        List<Replacement> randomReplacements = StringUtils.isBlank(word)
                ? replacementRepository.findRandomByStatus(ReplacementStatus.TO_REVIEW, pagination)
                : replacementRepository.findRandomByWordAndStatus(word, ReplacementStatus.TO_REVIEW, pagination);
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
        Collections.sort(articleReplacements);
        return articleReplacements;
    }

    public void deleteArticles(Set<Integer> articleIds) {
        replacementRepository.deleteByArticleIdIn(articleIds);
    }

    private void deleteArticle(int articleId) {
        replacementRepository.deleteByArticleIdIn(Collections.singleton(articleId));
    }

    public void deleteNotReviewedReplacements(int articleId) {
        replacementRepository.deleteByArticleIdAndStatus(articleId, ReplacementStatus.TO_REVIEW);
    }

    public List<Replacement> findAllReplacementsWithArticleIdGreaterThan(Integer minArticleId, int numArticles) {
        return replacementRepository.findByArticleIdGreaterThanOrderById(minArticleId, PageRequest.of(0, numArticles));
    }

    private boolean checkWordExistsInReplacements(@Nullable String word, List<ArticleReplacement> replacements) {
        if (StringUtils.isBlank(word)) {
            return true;
        } else {
            if (replacements.stream().noneMatch(replacement -> word.equals(replacement.getSubtype()))) {
                LOGGER.info("The filter word is not contained in the article replacements");
                return false;
            }
            return true;
        }
    }

    /**
     * Saves in Wikipedia the changes on an article validated in the front-end.
     */
    boolean saveArticleChanges(String title, String text, OAuth1AccessToken accessToken) {
        try {
            // Upload new content to Wikipedia
            wikipediaService.savePageContent(title, text, LocalDateTime.now(), accessToken);

            // Mark article as reviewed in the database
            markArticleAsReviewed(title);

            return true;
        } catch (WikipediaException e) {
            LOGGER.error("Error saving article: {}", title, e);
            return false;
        }
    }

    boolean markArticleAsReviewed(String articleTitle) {
        LOGGER.info("Mark article as reviewed: {}", articleTitle);
        // For the moment we need to retrieve the ID from the title
        try {
            wikipediaService.getPageByTitle(articleTitle).ifPresent(page ->
                    replacementRepository.findByArticleIdAndStatus(page.getId(), ReplacementStatus.TO_REVIEW)
                            .forEach(this::fixReplacement));
            return true;
        } catch (WikipediaException e) {
            LOGGER.error("Error marking article as reviewed: {}", articleTitle, e);
            return false;
        }
    }

    private void fixReplacement(Replacement replacement) {
        replacementRepository.save(replacement
                .withStatus(ReplacementStatus.FIXED)
                .withReviewer(DEFAULT_REVIEWER)
                .withLastUpdate(LocalDate.now()));
    }

    long countReplacements() {
        return replacementRepository.count();
    }

    long countReplacementsReviewed() {
        return replacementRepository.countByStatusIn(
                new HashSet<>(Arrays.asList(ReplacementStatus.REVIEWED, ReplacementStatus.FIXED)));
    }

    long countReplacementsToReview() {
        return replacementRepository.countByStatus(ReplacementStatus.TO_REVIEW);
    }

    List<ReplacementCount> findMisspellingsGrouped() {
        return replacementRepository.findMisspellingsGrouped();
    }

}
