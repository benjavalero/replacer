package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.cosmetic.CosmeticChangesService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("api/article")
public class ArticleController {

    @Autowired
    private ArticleReviewNoTypeService articleReviewNoTypeService;

    @Autowired
    private ArticleReviewTypeSubtypeService articleReviewTypeSubtypeService;

    @Autowired
    private ArticleReviewCustomService articleReviewCustomService;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private CosmeticChangesService cosmeticChangesService;

    /* FIND RANDOM ARTICLES WITH REPLACEMENTS */

    @GetMapping(value = "/random")
    public Optional<ArticleReview> findRandomArticleWithReplacements() {
        LOGGER.info("GET Find random article with replacements");
        return articleReviewNoTypeService.findRandomArticleReview();
    }

    @GetMapping(value = "/random/{type}/{subtype}")
    public Optional<ArticleReview> findRandomArticleByTypeAndSubtype(
            @PathVariable("type") String type, @PathVariable("subtype") String subtype) {
        LOGGER.info("GET Find random article with replacements. Type: {} - Subtype: {}", type, subtype);
        return articleReviewTypeSubtypeService.findRandomArticleReview(type, subtype);
    }

    @GetMapping(value = "/random/Personalizado/{subtype}/{suggestion}")
    public Optional<ArticleReview> findRandomArticleByCustomReplacement(
            @PathVariable("subtype") String replacement, @PathVariable("suggestion") String suggestion) {
        LOGGER.info("GET Find random article with replacements. Custom replacement: {} - {}", replacement, suggestion);
        return articleReviewCustomService.findRandomArticleReview(replacement, suggestion);
    }

    /* FIND AN ARTICLE REVIEW */

    @GetMapping(value = "/{id}")
    public Optional<ArticleReview> findArticleReviewById(@PathVariable("id") int articleId) {
        LOGGER.info("GET Find review for article. ID: {}", articleId);
        return articleReviewNoTypeService.getArticleReview(articleId);
    }

    @GetMapping(value = "/{id}/{type}/{subtype}")
    public Optional<ArticleReview> findArticleReviewByIdByTypeAndSubtype(
            @PathVariable("id") int articleId, @PathVariable("type") String type, @PathVariable("subtype") String subtype) {
        LOGGER.info("GET Find review for article. ID: {} - Type: {} - Subtype: {}", articleId, type, subtype);
        return articleReviewTypeSubtypeService.getArticleReview(articleId, type, subtype);
    }

    @GetMapping(value = "/{id}/Personalizado/{subtype}/{suggestion}")
    public Optional<ArticleReview> findArticleReviewByIdAndCustomReplacement(
            @PathVariable("id") int articleId, @PathVariable("subtype") String replacement, @PathVariable("suggestion") String suggestion) {
        LOGGER.info("GET Find review for article by custom type. ID: {} - Replacement: {} - Suggestion: {}",
                articleId, replacement, suggestion);
        return articleReviewCustomService.getArticleReview(articleId, replacement, suggestion);
    }

    /* SAVE CHANGES */

    @PutMapping
    public void save(@RequestParam("id") int articleId, @RequestBody String text,
                     @RequestParam String type, @RequestParam String subtype,
                     @RequestParam String reviewer, @RequestParam String currentTimestamp, @RequestParam @Nullable Integer section,
                     @RequestParam String token, @RequestParam String tokenSecret) throws WikipediaException {
        boolean changed = StringUtils.isNotBlank(text);
        LOGGER.info("PUT Save article. ID: {} - Changed: {}", articleId, changed);
        if (changed) {
            OAuth1AccessToken accessToken = new OAuth1AccessToken(token, tokenSecret);

            // Upload new content to Wikipedia
            String textToSave = cosmeticChangesService.applyCosmeticChanges(text);
            wikipediaService.savePageContent(articleId, textToSave, section, currentTimestamp, accessToken);
        }

        // Mark article as reviewed in the database
        replacementIndexService.reviewArticleReplacements(articleId, type, subtype, reviewer);
    }

}
