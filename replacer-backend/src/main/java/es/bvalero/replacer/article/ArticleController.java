package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping(value = "/article/random")
    public ArticleReview findRandomArticleWithReplacements() {
        LOGGER.info("Finding random article with replacements...");
        try {
            return articleService.findRandomArticleToReview();
        } catch (UnfoundArticleException e) {
            return ArticleReview.builder().setTitle(e.getMessage()).build();
        }
    }

    @GetMapping(value = "/article/random/{word}")
    public ArticleReview findRandomArticleByWord(@PathVariable("word") String word) {
        LOGGER.info("Finding random article by word: {}", word);
        try {
            return articleService.findRandomArticleToReview(word);
        } catch (UnfoundArticleException e) {
            return ArticleReview.builder().setTitle(e.getMessage()).build();
        }
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

    /* STATISTICS */

    @GetMapping(value = "/statistics/count/replacements")
    public Long countReplacements() {
        LOGGER.info("Count replacements...");
        Long count = articleService.countReplacements();
        LOGGER.info("Replacements found: {}", count);
        return count;
    }

    @GetMapping(value = "/statistics/count/articles")
    public Long countReplacementsToReview() {
        LOGGER.info("Count replacements not reviewed...");
        Long count = articleService.countReplacementsToReview();
        LOGGER.info("Replacements not reviewed found: {}", count);
        return count;
    }

    @GetMapping("/statistics/count/articles-reviewed")
    public Long countReplacementsReviewed() {
        LOGGER.info("Count replacements reviewed...");
        Long count = articleService.countReplacementsReviewed();
        LOGGER.info("Replacements reviewed found: {}", count);
        return count;
    }

    @GetMapping(value = "/statistics/count/misspellings")
    List<ReplacementCount> listMisspellings() {
        LOGGER.info("Listing misspellings...");
        List<ReplacementCount> list = articleService.findMisspellingsGrouped();
        LOGGER.info("Misspelling list found: {}", list.size());
        return list;
    }

}
