package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.Article;
import es.bvalero.replacer.article.ArticleRepository;
import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.PotentialError;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.RegexMatchType;
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

    // TODO Move to an enumerate in Wikipedia package
    private static final Integer NAMESPACE_ARTICLE = 0;
    private static final Integer NAMESPACE_ANNEX = 104;

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
    void processArticle(DumpArticle dumpArticle) {
        LOGGER.debug("Indexing article: {}...", dumpArticle.getTitle());

        // Check if it is really needed to process the article

        if (!NAMESPACE_ARTICLE.equals(dumpArticle.getNamespace()) && !NAMESPACE_ANNEX.equals(dumpArticle.getNamespace())) {
            LOGGER.debug("Only articles and annexes are processed. Skipping namespace: {}", dumpArticle.getNamespace());
            return;
        } else if (articleService.isRedirectionArticle(dumpArticle.getContent())) {
            LOGGER.debug("Redirection article. Skipping.");
            return;
        }

        article = articleRepository.findOne(dumpArticle.getId());

        if (article != null) {
            // TODO Add a flag to re-process again old articles. We avoid processing old articles but we miss new checks.
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

            for (RegexMatch regexMatch : regexMatches) {
                if (!RegexMatchType.EXCEPTION.equals(regexMatch.getType())) {
                    article.getPotentialErrors().add(
                            new PotentialError(article, regexMatch.getType(), regexMatch.getOriginalText()));
                }
            }

            articleRepository.save(article);
        }
    }

}
