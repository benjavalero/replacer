package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.persistence.Article;
import es.bvalero.replacer.persistence.ArticleRepository;
import es.bvalero.replacer.persistence.Replacement;
import es.bvalero.replacer.persistence.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Process an article found in a Wikipedia dump.
 */
@Component
class DumpArticleProcessor {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpArticleProcessor.class);

    private static final Collection<WikipediaNamespace> PROCESSABLE_NAMESPACES =
            EnumSet.of(WikipediaNamespace.ARTICLE, WikipediaNamespace.ANNEX);

    private static final int CACHE_SIZE = 1000;
    // Load a bunch of articles from DB to improve performance
    private final Map<Integer, Article> articlesDb = new HashMap<>(CACHE_SIZE);
    // Save articles in batches to improve performance
    private final Collection<Article> articlesToDelete = new ArrayList<>(CACHE_SIZE);
    private final Collection<Article> articlesToSave = new ArrayList<>(CACHE_SIZE);
    private final Collection<Replacement> replacementsToDelete = new ArrayList<>(CACHE_SIZE);
    // Prepare a Set to avoid duplicates
    private final Collection<Replacement> replacementsToAdd = new HashSet<>(CACHE_SIZE);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ArticleService articleService;

    private int maxCachedId;

    /**
     * Process a dump article: find the replacements and add them to the database.
     */
    @TestOnly
    boolean processArticle(DumpArticle dumpArticle) {
        return processArticle(dumpArticle, false);
    }

    /**
     * Process a dump article: find the replacements and add them to the database.
     */
    boolean processArticle(DumpArticle dumpArticle, boolean forceProcess) {
        LOGGER.debug("Processing article: {}...", dumpArticle.getTitle());

        // Check namespace and redirection articles
        if (!isDumpArticleProcessable(dumpArticle)) {
            return false;
        }

        // Load the cache of database articles
        if (articlesDb.isEmpty()) {
            for (Article article : articleRepository.findByIdGreaterThanOrderById(dumpArticle.getId() - 1, PageRequest.of(0, CACHE_SIZE))) {
                articlesDb.put(article.getId(), article);
                maxCachedId = article.getId();
            }
        }

        // Find the article in the cache
        Optional<Article> dbArticle = Optional.ofNullable(articlesDb.get(dumpArticle.getId()));

        // If the article is in the cache we can remove it from the cache
        dbArticle.ifPresent(article -> articlesDb.remove(article.getId()));

        // Clear the cache if obsolete (we assume the dump articles are in order)
        if (dumpArticle.getId() >= maxCachedId) {
            // The remaining cached articles are not in the dump so we remove them from DB
            articlesToDelete.addAll(articlesDb.values());
            articlesDb.clear();
        }

        if (dbArticle.isPresent() && !forceProcess) {
            // Check if reviewed after dump article timestamp
            // We compare with seconds because the DB timestamps is milliseconds-level but the Dump date is not
            // and it is possible that the review date and the timestamp is exactly the same
            if (dbArticle.get().getReviewDate() != null &&
                    !dbArticle.get().getReviewDate().truncatedTo(ChronoUnit.SECONDS)
                            .isBefore(dumpArticle.getTimestamp().truncatedTo(ChronoUnit.SECONDS))) {
                LOGGER.debug("Article reviewed after dump timestamp. Skipping.");
                return false;
            }

            // Check if added after dump article timestamp
            if (dbArticle.get().getAdditionDate().truncatedTo(ChronoUnit.SECONDS)
                    .isAfter(dumpArticle.getTimestamp().truncatedTo(ChronoUnit.SECONDS))) {
                LOGGER.debug("Article added after dump timestamp. Skipping.");
                return false;
            }
        }

        // Find replacements
        List<ArticleReplacement> articleReplacements = articleService.findReplacements(dumpArticle.getContent());

        if (articleReplacements.isEmpty()) {
            LOGGER.debug("No errors found in article: {}", dumpArticle.getTitle());
            dbArticle.ifPresent(article -> {
                replacementRepository.deleteByArticleId(article.getId()); // No need to delete in batch
                articlesToDelete.add(article);
            });
        } else if (dbArticle.isPresent()) {
            // Compare the new replacements with the existing ones
            Collection<Replacement> newReplacements = new HashSet<>(articleReplacements.size());
            articleReplacements.forEach(replacement -> newReplacements.add(Replacement.builder()
                    .setArticleId(dbArticle.get().getId())
                    .setType(replacement.getType())
                    .setText(replacement.getSubtype())
                    .build()));

            // To know if any real has been changed in the replacements in DB
            boolean modified = false;

            // Replacements to remove from DB
            List<Replacement> oldReplacements = replacementRepository.findByArticleId(dbArticle.get().getId());

            for (Replacement oldReplacement : oldReplacements) {
                if (!newReplacements.contains(oldReplacement)) {
                    replacementsToDelete.add(oldReplacement);
                    modified = true;
                }
            }

            // Replacements to add to DB
            for (Replacement newReplacement : newReplacements) {
                if (!oldReplacements.contains(newReplacement)) {
                    replacementsToAdd.add(newReplacement);
                    modified = true;
                }
            }

            if (modified) {
                Article articleToSave = dbArticle.get()
                        .withTitle(dumpArticle.getTitle()) // In case the title of the article has changed
                        .withAdditionDate(LocalDateTime.now())
                        .withReviewDate(null);
                articlesToSave.add(articleToSave);
            }
        } else {
            // New article to insert in DB
            Article newArticle = Article.builder()
                    .setId(dumpArticle.getId())
                    .setTitle(dumpArticle.getTitle())
                    .build();
            articlesToSave.add(newArticle);

            // Add replacements in DB
            articleReplacements.forEach(replacement -> replacementsToAdd.add(Replacement.builder()
                    .setArticleId(newArticle.getId())
                    .setType(replacement.getType())
                    .setText(replacement.getSubtype())
                    .build()));
        }

        return true;
    }

    private boolean isDumpArticleProcessable(DumpArticle dumpArticle) {
        // Check namespace and redirection articles
    	return PROCESSABLE_NAMESPACES.contains(dumpArticle.getNamespace())
    			&& !WikipediaUtils.isRedirectionArticle(dumpArticle.getContent());
    }

    
    private void flushModifications() {
        // There is a FK Replacement-Article, we need to remove the replacements first.
        if (!replacementsToDelete.isEmpty()) {
            replacementRepository.deleteInBatch(replacementsToDelete);
            replacementsToDelete.clear();
        }

        if (!articlesToDelete.isEmpty()) {
            articleRepository.deleteInBatch(articlesToDelete);
            articlesToDelete.clear();
        }

        if (!articlesToSave.isEmpty()) {
            articleRepository.saveAll(articlesToSave);
            articlesToSave.clear();
        }

        if (!replacementsToAdd.isEmpty()) {
            replacementRepository.saveAll(replacementsToAdd);
            replacementsToAdd.clear();
        }

        // Flush and clear to avoid memory leaks (we are performing millions of updates)
        articleRepository.flush();
        replacementRepository.flush();
        articleRepository.clear(); // This clears all the EntityManager
    }

    void finish() {
        // The remaining cached articles are not in the dump so we remove them from DB
        articlesToDelete.addAll(articlesDb.values());

        flushModifications();
    }

}
