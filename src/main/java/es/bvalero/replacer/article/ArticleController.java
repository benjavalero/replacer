package es.bvalero.replacer.article;

import es.bvalero.replacer.utils.StringUtils;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class ArticleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    @Autowired
    private IWikipediaFacade wikipediaFacade;

    @Autowired
    private ArticleRepository articleRepository;

    /**
     * @return An random article whose text contains potential errors.
     */
    @RequestMapping(value = "/article/random")
    public ArticleData random() {
        LOGGER.info("Finding random article with potential errors...");
        return articleService.findRandomArticleWithPotentialErrors();
    }

    @RequestMapping(value = "/article/save")
    public boolean save(@RequestBody ArticleData article) {
        LOGGER.info("Saving changes in: {}", article.getTitle());

        // Find the fixes verified by the user
        List<ArticleReplacement> userFixes = new ArrayList<>();
        for (ArticleReplacement replacement : article.getFixes().values()) {
            if (replacement.isFixed()) {
                userFixes.add(replacement);
            }
        }
        Collections.sort(userFixes);

        if (userFixes.isEmpty()) {
            LOGGER.info("Nothing to fix in article: {}", article.getTitle());
            markArticleAsReviewed(article);
            return true;
        }

        // Apply the fixes
        String currentContent;
        try {
            currentContent = wikipediaFacade.getArticleContent(article.getTitle());
        } catch (Exception e) {
            LOGGER.error("Error getting the current content of the article: " + article.getTitle(), e);
            return false;
        }
        // Escape the content just in case it contains XML tags
        String replacedContent = StringUtils.escapeText(currentContent);
        try {
            for (ArticleReplacement fix : userFixes) {
                LOGGER.debug("Fixing article {}: {} -> {}",
                        article.getTitle(), fix.getOriginalText(), fix.getFixedText());
                replacedContent = StringUtils.replaceAt(replacedContent, fix.getPosition(),
                        fix.getOriginalText(), fix.getFixedText());
            }
            String contentToUpload = StringUtils.unescapeText(replacedContent);

            // Upload the new content to Wikipedia
            // It may happen there has been changes during the edition, but in this point the fixes can be applied anyway.
            // Check just before uploading there are no changes during the edition
            if (contentToUpload.equals(currentContent)) {
                LOGGER.warn("The content to upload matches with the current content");
                markArticleAsReviewed(article);
                return false;
            }
            // TODO Try to add a reference in the summary to the tool
            wikipediaFacade.editArticleContent(article.getTitle(), contentToUpload,
                    "Correcciones ortográficas");

            // Mark the article has reviewed in the database
            markArticleAsReviewed(article);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("The article content could not be replaced with the fixes", e);
        } catch (Exception e) {
            LOGGER.error("The article fixed content could not be uploaded for unknown reasons", e);
        }

        // Return a new article to check
        return true;
    }

    private void markArticleAsReviewed(ArticleData article) {
        articleRepository.setArticleAsReviewed(article.getId());
    }

}
