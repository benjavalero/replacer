package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.*;
import org.apache.commons.collections4.CollectionUtils;
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

    /**
     * Process a dump article: find the potential errors and add them to the database.
     */
    void processArticle(@NotNull DumpArticle dumpArticle, boolean processOldArticles) {
        LOGGER.debug("Indexing article: {}...", dumpArticle.getTitle());

        Article article = articleRepository.findOne(dumpArticle.getId());

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
            addPotentialErrorsToArticle(article, articleReplacements);

            articleRepository.save(article);
        }
    }

    private void addPotentialErrorsToArticle(Article article, List<ArticleReplacement> articleReplacements) {
        Set<PotentialError> potentialErrorSet = new HashSet<>();
        for (ArticleReplacement articleReplacement : articleReplacements) {
            PotentialError potentialError = new PotentialError(articleReplacement.getType(), articleReplacement.getSubtype());
            potentialErrorSet.add(potentialError);
        }

        Collection<PotentialError> toRemove = CollectionUtils.subtract(article.getPotentialErrors(), potentialErrorSet);
        Collection<PotentialError> toAdd = CollectionUtils.subtract(potentialErrorSet, article.getPotentialErrors());

        for (PotentialError potentialError : toRemove) {
            article.removePotentialError(potentialError);
        }

        for (PotentialError potentialError : toAdd) {
            article.addPotentialError(potentialError);
        }
    }

}
