package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.authentication.AccessToken;
import es.bvalero.replacer.finder.CosmeticFindService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private CosmeticFindService cosmeticFindService;

    /* FIND RANDOM ARTICLES WITH REPLACEMENTS */

    @GetMapping(value = "/random")
    public Optional<ArticleReview> findRandomArticleWithReplacements() {
        LOGGER.info("GET Find random article");
        return articleReviewNoTypeService.findRandomArticleReview();
    }

    @GetMapping(value = "/random/{type}/{subtype}")
    public Optional<ArticleReview> findRandomArticleByTypeAndSubtype(
            @PathVariable("type") String type, @PathVariable("subtype") String subtype) {
        LOGGER.info("GET Find random article. Type: {} - {}", type, subtype);
        return articleReviewTypeSubtypeService.findRandomArticleReview(type, subtype);
    }

    @GetMapping(value = "/random/Personalizado/{subtype}/{suggestion}")
    public Optional<ArticleReview> findRandomArticleByCustomReplacement(
            @PathVariable("subtype") String replacement, @PathVariable("suggestion") String suggestion) {
        LOGGER.info("GET Find random article. Custom replacement: {} - {}", replacement, suggestion);
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

    @PostMapping
    public void save(@RequestBody SaveArticle saveArticle) throws WikipediaException {
        boolean changed = StringUtils.isNotBlank(saveArticle.getContent());
        LOGGER.info("PUT Save article. ID: {} - Changed: {}", saveArticle.getArticleId(), changed);
        if (changed) {
            // Upload new content to Wikipedia
            String textToSave = cosmeticFindService.applyCosmeticChanges(saveArticle.getContent());
            wikipediaService.savePageContent(saveArticle.getArticleId(), textToSave, saveArticle.getSection(),
                    saveArticle.getTimestamp(), convertToEntity(saveArticle.getToken()));
        }

        // Mark article as reviewed in the database
        replacementIndexService.reviewArticleReplacements(saveArticle.getArticleId(), saveArticle.getType(),
                saveArticle.getSubtype(), saveArticle.getReviewer());
    }

    private OAuth1AccessToken convertToEntity(AccessToken accessToken) {
        return new OAuth1AccessToken(accessToken.getToken(), accessToken.getTokenSecret());
    }


}
