package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("article")
public class ArticleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    /**
     * @return A random article whose text contains replacements to review.
     * In case of error we return a fake article with the exception message.
     */
    @GetMapping(value = "/random")
    public List<Integer> findRandomArticleWithReplacements() {
        LOGGER.info("GET Find random article with replacements");
        return articleService.findRandomArticleToReview()
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    @GetMapping(value = "/random/{word}")
    public List<Integer> findRandomArticleByWord(@PathVariable("word") String word) {
        LOGGER.info("GET Find random article with replacements by word: {}", word);
        return articleService.findRandomArticleToReview(word)
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    @GetMapping(value = "/review/{id}")
    public Optional<ArticleReview> findArticleReviewById(@PathVariable("id") int articleId) {
        LOGGER.info("GET Find replacements for article: {}", articleId);
        return articleService.findArticleReviewById(articleId);
    }

    @GetMapping(value = "/review/{id}/{word}")
    public Optional<ArticleReview> findArticleReviewById(@PathVariable("id") int articleId,
                                                         @PathVariable("word") String word) {
        LOGGER.info("GET Find replacements for article {} by word: {}", articleId, word);
        return articleService.findArticleReviewById(articleId, word);
    }

    @PutMapping
    public boolean save(@RequestParam("id") int articleId, @RequestBody String text, @RequestParam String reviewer,
                        @RequestParam String lastUpdate, @RequestParam String currentTimestamp,
                        @RequestParam String token, @RequestParam String tokenSecret) throws WikipediaException {
        LOGGER.info("PUT Save article with ID: {}", articleId);
        if (StringUtils.isNotBlank(text)) {
            OAuth1AccessToken accessToken = new OAuth1AccessToken(token, tokenSecret);
            articleService.saveArticleChanges(articleId, text, reviewer, lastUpdate, currentTimestamp, accessToken);
        } else {
            LOGGER.info("No changes in article. Mark directly as reviewed: {}", articleId);
            articleService.markArticleAsReviewed(articleId, reviewer);
        }
        return true;
    }

    /* STATISTICS */

    @GetMapping(value = "/count/replacements")
    public Long countReplacements() {
        LOGGER.info("GET Count replacements");
        Long count = articleService.countReplacements();
        LOGGER.info("Replacements found: {}", count);
        return count;
    }

    @GetMapping(value = "/count/replacements/to-review")
    public Long countReplacementsToReview() {
        LOGGER.info("GET Count replacements not reviewed");
        Long count = articleService.countReplacementsToReview();
        LOGGER.info("Replacements found not reviewed: {}", count);
        return count;
    }

    @GetMapping("/count/replacements/reviewed")
    public Long countReplacementsReviewed() {
        LOGGER.info("GET Count replacements reviewed");
        Long count = articleService.countReplacementsReviewed();
        LOGGER.info("Replacements found reviewed: {}", count);
        return count;
    }

    @GetMapping(value = "/count/misspellings")
    public List<ReplacementCount> listMisspellings() {
        LOGGER.info("GET List misspellings");
        List<ReplacementCount> list = articleService.findMisspellingsGrouped();
        LOGGER.info("Misspelling list found: {}", list.size());
        return list;
    }

}
