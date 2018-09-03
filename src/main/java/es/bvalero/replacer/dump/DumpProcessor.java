package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

@Component
class DumpProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpProcessor.class);
    private static final int CACHE_SIZE = 1000;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleService articleService;

    // Load a bunch of articles from DB to improve performance
    private Map<Integer, Article> articlesDb = new HashMap<>(CACHE_SIZE);
    private int maxIdDb = 0;

    // Save articles in batches to improve performance
    private List<Article> articlesToDelete = new ArrayList<>();
    private List<Article> articlesToSave = new ArrayList<>();

    /**
     * Process a dump article: find the potential errors and add them to the database.
     */
    void processArticle(@NotNull DumpArticle dumpArticle, DumpStatus dumpStatus) {
        LOGGER.debug("Indexing article: {}...", dumpArticle.getTitle());

        long startReadDbTime = System.currentTimeMillis();
        Article article = articlesDb.get(dumpArticle.getId());
        if (article == null && dumpArticle.getId() > maxIdDb) {
            // The remaining cached articles are not in the dump so we remove them
            articlesToDelete.addAll(articlesDb.values());

            flushModifications();

            // The list of DB articles to cache is ordered by ID
            articlesDb.clear();

            for (Article articleDb : articleRepository
                    .findByIdGreaterThanOrderById(dumpArticle.getId() - 1, new PageRequest(0, CACHE_SIZE))) {
                articlesDb.put(articleDb.getId(), articleDb);
                maxIdDb = articleDb.getId();
            }
            article = articlesDb.get(dumpArticle.getId());
        }
        long readDbTime = System.currentTimeMillis() - startReadDbTime;

        if (article != null) {
            // The article in dump exists also already in the database
            articlesDb.remove(article.getId());

            if (article.getReviewDate() != null && !dumpArticle.getTimestamp().after(article.getReviewDate())) {
                LOGGER.debug("Article reviewed after dump timestamp. Skipping.");
                return;
            } else if (dumpArticle.getTimestamp().before(article.getAdditionDate()) && !dumpStatus.isProcessOldArticles()) {
                LOGGER.debug("Article added after dump timestamp. Skipping.");
                return;
            }
        }

        // Process the article. Find the potential errors ignoring the ones in exceptions.
        long startRegexTime = System.currentTimeMillis();
        List<ArticleReplacement> articleReplacements = articleService.findPotentialErrorsIgnoringExceptions(dumpArticle.getContent());
        long regexTime = System.currentTimeMillis() - startRegexTime;

        long startWriteDbTime = System.currentTimeMillis();
        if (articleReplacements.isEmpty()) {
            LOGGER.debug("No errors found in article: {}", dumpArticle.getTitle());
            if (article != null) {
                articlesToDelete.add(article);
            }
        } else {
            if (article == null) {
                article = new Article(dumpArticle.getId(), dumpArticle.getTitle());
            } else {
                article.setTitle(dumpArticle.getTitle()); // In case the title of the article has changed
            }

            article.setAdditionDate(new Timestamp(System.currentTimeMillis()));
            article.setReviewDate(null);
            if (addPotentialErrorsToArticle(article, articleReplacements)) {
                // Only save if there are modifications in the potential errors found for the article
                articlesToSave.add(article);
            }
        }
        long writeDbTime = System.currentTimeMillis() - startWriteDbTime;

        // Update the status
        dumpStatus.increaseArticles();
        dumpStatus.increaseArticleTime(readDbTime, regexTime, writeDbTime);
    }

    /* Add the new errors found to the article and also remove the obsolete ones */
    private boolean addPotentialErrorsToArticle(Article article, List<ArticleReplacement> articleReplacements) {
        Set<PotentialError> newPotentialErrors = new HashSet<>(articleReplacements.size());
        for (ArticleReplacement articleReplacement : articleReplacements) {
            PotentialError potentialError = new PotentialError(articleReplacement.getType(), articleReplacement.getSubtype());
            newPotentialErrors.add(potentialError);
        }

        boolean isModified = false;

        // Remove the obsolete potential errors from the article
        Iterator<PotentialError> it = article.getPotentialErrors().iterator();
        while (it.hasNext()) {
            PotentialError oldPotentialError = it.next();
            if (!newPotentialErrors.contains(oldPotentialError)) {
                it.remove();
                isModified = true;
            }
        }

        // Add the new potential errors to the article
        for (PotentialError newPotentialError : newPotentialErrors) {
            if (!article.getPotentialErrors().contains(newPotentialError)) {
                article.addPotentialError(newPotentialError);
                isModified = true;
            }
        }

        return isModified;
    }

    private void flushModifications() {
        // Save all modifications
        if (!articlesToDelete.isEmpty()) {
            articleRepository.delete(articlesToDelete);
            articlesToDelete.clear();
        }

        if (!articlesToSave.isEmpty()) {
            articleRepository.save(articlesToSave);
            articlesToSave.clear();
        }

        // Flush and clear to avoid memory leaks (we are performing millions of updates)
        articleRepository.flush();
        articleRepository.clear();
    }

    void finish() {
        flushModifications();
    }

}
