package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ArticleReview findRandomArticleWithReplacements() {
        LOGGER.info("GET Find random article with replacements");
        try {
            return articleService.findRandomArticleToReview();
        } catch (UnfoundArticleException e) {
            LOGGER.warn("No article found with replacements");
            return ArticleReview.builder().setTitle(e.getMessage()).build();
        }
    }

    @GetMapping(value = "/random/{word}")
    public ArticleReview findRandomArticleByWord(@PathVariable("word") String word) {
        LOGGER.info("GET Find random article with replacements by word: {}", word);
        try {
            return articleService.findRandomArticleToReview(word);
        } catch (UnfoundArticleException e) {
            LOGGER.info("No article found with replacements by word: {}", word);
            return ArticleReview.builder().setTitle(e.getMessage()).build();
        }
    }

    @PutMapping(value = "/")
    public boolean save(@RequestParam String title, @RequestBody String text,
                        @RequestParam String token, @RequestParam String tokenSecret) {
        LOGGER.info("PUT Save article with title: {}", title);
        if (StringUtils.isNotBlank(text)) {
            OAuth1AccessToken accessToken = new OAuth1AccessToken(token, tokenSecret);
            return articleService.saveArticleChanges(title, text, accessToken);
        } else {
            LOGGER.info("No changes in article. Mark directly as reviewed: {}", title);
            return articleService.markArticleAsReviewed(title);
        }
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
