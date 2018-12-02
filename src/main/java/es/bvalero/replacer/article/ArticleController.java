package es.bvalero.replacer.article;

import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleController {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    /**
     * @return A random article whose text contains replacements to review.
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

    @RequestMapping("/article/save")
    public boolean save(@RequestParam("title") String title, @RequestParam("text") String text) {
        LOGGER.info("Saving changes in: {}", title);
        return articleService.saveArticleChanges(title, text);
    }

    @RequestMapping("/article/save/nochanges")
    public boolean saveNoChanges(@RequestParam("title") String title) {
        LOGGER.info("Saving with no changes: {}", title);
        return articleService.markArticleAsReviewed(title);
    }

}
