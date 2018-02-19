package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.Article;
import es.bvalero.replacer.article.ArticleRepository;
import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.PotentialError;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.article.PotentialErrorType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Component
class DumpProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpProcessor.class);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleService articleService;

    // Having this variable outside the method we try to improve the garbage collector
    @SuppressWarnings("FieldCanBeLocal")
    private Article article;

    /**
     * Process a dump article: find the potential errors and add them to the database.
     */
    void processArticle(@NotNull DumpArticle dumpArticle) {
        LOGGER.debug("Indexing article: {}...", dumpArticle.getTitle());

        // Check if it is really needed to process the article
        if (!dumpArticle.isProcessable()) {
            return;
        }

        article = articleRepository.findOne(dumpArticle.getId());

        if (article != null) {
            if (article.getReviewDate() != null && !dumpArticle.getTimestamp().after(article.getReviewDate())) {
                LOGGER.debug("Article reviewed after dump timestamp. Skipping.");
                return;
            } else if (dumpArticle.getTimestamp().before(article.getAdditionDate())) {
                LOGGER.debug("Article added after dump timestamp. Skipping.");
                return;
            }
        }

        // Process the article. Find the potential errors ignoring the ones in exceptions.

        List<RegexMatch> regexMatches = articleService.findPotentialErrorsAndExceptions(dumpArticle.getContent());
        if (regexMatches.isEmpty()) {
            LOGGER.debug("No errors found in article: {}", dumpArticle.getTitle());
            if (article != null) {
                articleRepository.delete(article);
            }
        } else {
            if (article == null) {
                article = new Article(dumpArticle.getId(), dumpArticle.getTitle());
            } else {
                article.getPotentialErrors().clear();
            }

            article.setAdditionDate(new Timestamp(new Date().getTime()));
            addPotentialErrorsToArticle(article, regexMatches);

            articleRepository.save(article);
        }
    }

    private void addPotentialErrorsToArticle(Article article, List<RegexMatch> regexMatches) {
        for (RegexMatch regexMatch : regexMatches) {
            if (!PotentialErrorType.EXCEPTION.equals(regexMatch.getType())) {
                article.getPotentialErrors().add(
                        new PotentialError(article, regexMatch.getType(), regexMatch.getOriginalText()));
            }
        }
    }

}
