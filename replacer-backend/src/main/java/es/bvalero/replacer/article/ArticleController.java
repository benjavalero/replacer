package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ArticleController {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    /**
     * @return A random article whose text contains replacements to review.
     * In case of error we return a fake article with the exception message.
     */
    @RequestMapping("/article/random")
    public ArticleReview findRandomArticleWithReplacements() {
        LOGGER.info("Finding random article with replacements...");

        ArticleReview articleData = null;
        do {
            try {
                articleData = articleService.findRandomArticleWithReplacements();
            } catch (UnfoundArticleException e) {
                articleData = ArticleReview.builder().setTitle(e.getMessage()).build();
            } catch (InvalidArticleException e) {
                // Retry
            }
        } while (articleData == null);

        return articleData;
    }

    /**
     * @return A random article whose text contains a specific error.
     */
    @RequestMapping("/article/random/{word}")
    public ArticleReview findRandomArticleByWord(@PathVariable("word") String word) {
        LOGGER.info("Finding random article by word: {}", word);

        ArticleReview articleData = null;
        do {
            try {
                articleData = articleService.findRandomArticleWithReplacements(word);
            } catch (UnfoundArticleException e) {
                articleData = ArticleReview.builder().setTitle(e.getMessage()).build();
            } catch (InvalidArticleException e) {
                // Retry
            }
        } while (articleData == null);

        return articleData;
    }

    @PutMapping("/article")
    public boolean save(@RequestParam String title, @RequestBody String text,
                     @RequestParam String token, @RequestParam String tokenSecret) {
        if (StringUtils.isNotBlank(text)) {
            OAuth1AccessToken accessToken = new OAuth1AccessToken(token, tokenSecret);
            return articleService.saveArticleChanges(title, text, accessToken);
        } else {
            LOGGER.info("Saving with no changes: {}", title);
            try {
                articleService.markArticleAsReviewed(title);
                return true;
            } catch (Exception e) {
                LOGGER.error("Error marking article as reviewed: {}", title);
                return false;
            }
        }
    }

}
