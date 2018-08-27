package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

@Component
class DumpProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpProcessor.class);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleService articleService;

    private Map<Integer, Article> articlesDb = new HashMap<>(1000);
    private int maxIdDb = 0;

    /**
     * Process a dump article: find the potential errors and add them to the database.
     */
    void processArticle(@NotNull DumpArticle dumpArticle, boolean processOldArticles) {
        LOGGER.debug("Indexing article: {}...", dumpArticle.getTitle());

        Article article = articlesDb.get(dumpArticle.getId());
        if (article == null && dumpArticle.getId() > maxIdDb) {
            // The list of DB articles to cache is ordered by ID
            articlesDb.clear();
            for (Article articleDb : articleRepository.findFirst1000ByIdGreaterThanOrderById(dumpArticle.getId() - 1)) {
                articlesDb.put(articleDb.getId(), articleDb);
                maxIdDb = articleDb.getId();
            }
            article = articlesDb.get(dumpArticle.getId());
        }

        if (article != null) {
            if (article.getReviewDate() != null && !dumpArticle.getTimestamp().after(article.getReviewDate())) {
                LOGGER.debug("Article reviewed after dump timestamp. Skipping.");
                return;
            } else if (dumpArticle.getTimestamp().before(article.getAdditionDate()) && !processOldArticles) {
                LOGGER.debug("Article added after dump timestamp. Skipping.");
                return;
            }
        }

        // Process the article. Find the potential errors ignoring the ones in exceptions.

        List<ArticleReplacement> articleReplacements = articleService.findPotentialErrorsIgnoringExceptions(dumpArticle.getContent());
        if (articleReplacements.isEmpty()) {
            LOGGER.debug("No errors found in article: {}", dumpArticle.getTitle());
            if (article != null) {
                articleRepository.delete(article);
            }
        } else {
            if (article == null) {
                article = new Article(dumpArticle.getId(), dumpArticle.getTitle());
            } else {
                article.setTitle(dumpArticle.getTitle()); // In case the title of the article has changed
            }

            article.setAdditionDate(new Timestamp(new Date().getTime()));
            if (addPotentialErrorsToArticle(article, articleReplacements)) {
                // Only save if there are modifications in the potential errors found for the article
                articleRepository.save(article);
            }
        }
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

}
