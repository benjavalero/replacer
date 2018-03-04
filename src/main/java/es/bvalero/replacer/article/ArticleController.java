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
        return articleService.findRandomArticleWithPotentialErrors();
    }

    /**
     * @return A random article whose text contains a specific error.
     */
    @RequestMapping(value = "/article/random/word/{word}")
    public ArticleData randomByWord(@PathVariable("word") String word) {
        LOGGER.info("Finding random article containing error: {}", word);
        return articleService.findRandomArticleWithPotentialErrors(word);
    }

    @RequestMapping(value = "/article/save")
    public boolean save(@RequestBody ArticleData article) {
        LOGGER.info("Saving changes in: {}", article.getTitle());
        return articleService.saveArticleChanges(article);
    }

}
