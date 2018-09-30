package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.*;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

/**
 * Process an article found in a Wikipedia dump.
 */
@Component
class DumpArticleProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpArticleProcessor.class);

    private static final Set<WikipediaNamespace> PROCESSABLE_NAMESPACES =
            new HashSet<>(Arrays.asList(WikipediaNamespace.ARTICLE, WikipediaNamespace.ANNEX));

    private static final int CACHE_SIZE = 1000;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private PotentialErrorRepository potentialErrorRepository;

    @Autowired
    private ArticleService articleService;

    // Load a bunch of articles from DB to improve performance
    private Map<Integer, Article> articlesDb = new HashMap<>(CACHE_SIZE);
    private int maxCachedId = 0;

    // Save articles in batches to improve performance
    private List<Article> articlesToDelete = new ArrayList<>(CACHE_SIZE);
    private List<Article> articlesToSave = new ArrayList<>(CACHE_SIZE);
    private List<PotentialError> replacementsToDelete = new ArrayList<>(CACHE_SIZE);
    private List<PotentialError> replacementsToAdd = new ArrayList<>(CACHE_SIZE);

    /**
     * Process a dump article: find the potential errors and add them to the database.
     */
    @TestOnly
    boolean processArticle(DumpArticle dumpArticle) {
        return processArticle(dumpArticle, false);
    }

    /**
     * Process a dump article: find the potential errors and add them to the database.
     */
    boolean processArticle(DumpArticle dumpArticle, boolean forceProcess) {
        LOGGER.debug("Processing article: {}...", dumpArticle.getTitle());

        // Check namespace
        if (!PROCESSABLE_NAMESPACES.contains(dumpArticle.getNamespace())) {
            return false;
        }

        // Check redirection articles
        if (WikipediaUtils.isRedirectionArticle(dumpArticle.getContent())) {
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
            if (dbArticle.get().getReviewDate() != null
                    && dbArticle.get().getReviewDate().getTime() / 1000 >= dumpArticle.getTimestamp().getTime() / 1000) {
                LOGGER.debug("Article reviewed after dump timestamp. Skipping.");
                return false;
            }

            // Check if added after dump article timestamp
            if (dbArticle.get().getAdditionDate() != null
                    && dbArticle.get().getAdditionDate().after(dumpArticle.getTimestamp())) {
                LOGGER.debug("Article added after dump timestamp. Skipping.");
                return false;
            }
        }

        // TODO Create interface for this
        List<ArticleReplacement> articleReplacements = articleService.findPotentialErrorsIgnoringExceptions(dumpArticle.getContent());

        if (articleReplacements.isEmpty()) {
            LOGGER.debug("No errors found in article: {}", dumpArticle.getTitle());
            dbArticle.ifPresent(article -> {
                potentialErrorRepository.deleteByArticle(article); // No need to delete in batch
                articlesToDelete.add(article);
            });
        } else if (dbArticle.isPresent()) {
            // Compare the new replacements with the existing ones
            Set<PotentialError> newPotentialErrors = new HashSet<>(articleReplacements.size());
            for (ArticleReplacement articleReplacement : articleReplacements) {
                PotentialError potentialError = new PotentialError.PotentialErrorBuilder()
                        .setArticle(dbArticle.get())
                        .setType(articleReplacement.getType())
                        .setText(articleReplacement.getSubtype())
                        .createPotentialError();
                newPotentialErrors.add(potentialError);
            }

            // To know if any real has been changed in the replacements in DB
            boolean modified = false;

            // Replacements to remove from DB
            List<PotentialError> oldPotentialErrors = potentialErrorRepository.findByArticle(dbArticle.get());

            for (PotentialError oldPotentialError : oldPotentialErrors) {
                if (!newPotentialErrors.contains(oldPotentialError)) {
                    replacementsToDelete.add(oldPotentialError);
                    modified = true;
                }
            }

            // Replacements to add to DB
            for (PotentialError newPotentialError : newPotentialErrors) {
                if (!oldPotentialErrors.contains(newPotentialError)) {
                    replacementsToAdd.add(newPotentialError);
                    modified = true;
                }
            }

            if (modified) {
                Article articleToSave = new Article.ArticleBuilder(dbArticle.get())
                        .setTitle(dumpArticle.getTitle()) // In case the title of the article has changed
                        .setAdditionDate(new Timestamp(System.currentTimeMillis()))
                        .setReviewDate(null)
                        .createArticle();
                articlesToSave.add(articleToSave);
            }
        } else {
            // New article to insert in DB
            Article newArticle = new Article.ArticleBuilder()
                    .setId(dumpArticle.getId())
                    .setTitle(dumpArticle.getTitle())
                    .createArticle();
            articlesToSave.add(newArticle);

            // Add replacements in DB
            for (ArticleReplacement articleReplacement : articleReplacements) {
                PotentialError newPotentialError = new PotentialError.PotentialErrorBuilder()
                        .setArticle(newArticle)
                        .setType(articleReplacement.getType())
                        .setText(articleReplacement.getSubtype())
                        .createPotentialError();
                replacementsToAdd.add(newPotentialError);
            }
        }

        return true;
    }

    private void flushModifications() {
        // There is a FK Replacement-Article, we need to remove the replacements first.
        if (!replacementsToDelete.isEmpty()) {
            potentialErrorRepository.deleteInBatch(replacementsToDelete);
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
            potentialErrorRepository.saveAll(replacementsToAdd);
            replacementsToAdd.clear();
        }

        // Flush and clear to avoid memory leaks (we are performing millions of updates)
        articleRepository.flush();
        potentialErrorRepository.flush();
        articleRepository.clear(); // This clears all the EntityManager
    }

    void finish() {
        // The remaining cached articles are not in the dump so we remove them from DB
        articlesToDelete.addAll(articlesDb.values());

        flushModifications();
    }

}
