package es.bvalero.replacer.article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    /**
     * @return A random article whose text contains potential errors.
     */
    @RequestMapping(value = "/article/random")
    public ArticleData random() {
        LOGGER.info("Finding random article with potential errors...");

        ArticleData articleData = null;
        do {
            try {
                articleData = articleService.findRandomArticleWithPotentialErrors();
            } catch (UnfoundArticleException e) {
                articleData = new ArticleData();
                articleData.setTitle("No hay artículos por revisar");
            } catch (InvalidArticleException e) {
                // Retry
            }
        } while (articleData == null);

        return articleData;
    }

    /**
     * @return A random article whose text contains a specific error.
     */
    @RequestMapping(value = "/article/random/word/{word}")
    public ArticleData randomByWord(@PathVariable("word") String word) {
        LOGGER.info("Finding random article containing error: {}", word);

        ArticleData articleData = null;
        do {
            try {
                articleData = articleService.findRandomArticleWithPotentialErrors(word);
            } catch (UnfoundArticleException e) {
                articleData = new ArticleData();
                articleData.setTitle("No hay artículos por revisar");
            } catch (InvalidArticleException e) {
                // Retry
            }
        } while (articleData == null);

        return articleData;
    }

    @RequestMapping(value = "/article/save")
    public boolean save(@RequestBody ArticleData article) {
        LOGGER.info("Saving changes in: {}", article.getTitle());
        return articleService.saveArticleChanges(article);
    }

}
