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
    private static final Integer NAMESPACE_ARTICLE = 0;
    private static final Integer NAMESPACE_ANNEX = 104;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleService articleService;

    void processArticle(Integer articleId, String articleContent, Integer articleNamespace, String articleTitle, Date articleTimestamp) {
        LOGGER.debug("Indexing article: {}...", articleTitle);

        if (!NAMESPACE_ARTICLE.equals(articleNamespace) && !NAMESPACE_ANNEX.equals(articleNamespace)) {
            LOGGER.debug("Only articles and annexes are processed. Skipping namespace: {}", articleNamespace);
            return;
        } else if (articleService.isRedirectionArticle(articleContent)) {
            LOGGER.debug("Redirection article. Skipping.");
            return;
        }

        Article article = articleRepository.findOne(articleId);

        if (article != null) {
            if (article.getReviewDate() != null && !articleTimestamp.after(article.getReviewDate())) {
                LOGGER.debug("Article reviewed after dump timestamp. Skipping.");
                return;
            } else if (articleTimestamp.before(article.getAdditionDate())) {
                LOGGER.debug("Article added after dump timestamp. Skipping.");
                // With this we avoid processing old articles but we will miss new checks
                // TODO Add a flag to re-process again old articles
                return;
            }
        }

        List<RegexMatch> regexMatches = articleService.findPotentialErrorsAndExceptions(articleContent);
        if (regexMatches.isEmpty()) {
            LOGGER.debug("No errors found in article: {}", articleTitle);
            if (article != null) {
                articleRepository.delete(article);
            }
        } else {
            if (article == null) {
                article = new Article(articleId, articleTitle);
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
