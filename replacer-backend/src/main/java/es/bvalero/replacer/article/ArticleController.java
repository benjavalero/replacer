package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.cosmetic.CosmeticChangesService;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleStatsService articleStatsService;

    @Autowired
    private ArticleIndexService articleIndexService;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private CosmeticChangesService cosmeticChangesService;

    /* FIND RANDOM ARTICLES WITH REPLACEMENTS */

    @GetMapping(value = "/random")
    public Optional<ArticleReview> findRandomArticleWithReplacements() {
        LOGGER.info("GET Find random article with replacements");
        return articleService.findRandomArticleToReview();
    }

    @GetMapping(value = "/random/{type}/{subtype}")
    public Optional<ArticleReview> findRandomArticleByTypeAndSubtype(
            @PathVariable("type") String type, @PathVariable("subtype") String subtype) {
        LOGGER.info("GET Find random article with replacements. Type: {} - Subtype: {}", type, subtype);
        return articleService.findRandomArticleToReview(type, subtype);
    }

    @GetMapping(value = "/random/Personalizado/{subtype}/{suggestion}")
    public Optional<ArticleReview> findRandomArticleByCustomReplacement(
            @PathVariable("subtype") String replacement, @PathVariable("suggestion") String suggestion) {
        LOGGER.info("GET Find random article with replacements. Custom replacement: {} - {}", replacement, suggestion);
        return articleService.findRandomArticleToReviewWithCustomReplacement(replacement, suggestion);
    }

    /* FIND AN ARTICLE REVIEW */

    @GetMapping(value = "/{id}")
    public Optional<ArticleReview> findArticleReviewById(@PathVariable("id") int articleId) {
        LOGGER.info("GET Find review for article. ID: {}", articleId);
        return articleService.findArticleReview(articleId, null, null, null);
    }

    @GetMapping(value = "/{id}/{type}/{subtype}")
    public Optional<ArticleReview> findArticleReviewByIdByTypeAndSubtype(
            @PathVariable("id") int articleId, @PathVariable("type") String type, @PathVariable("subtype") String subtype) {
        LOGGER.info("GET Find review for article. ID: {} - Type: {} - Subtype: {}", articleId, type, subtype);
        return articleService.findArticleReview(articleId, type, subtype, null);
    }

    @GetMapping(value = "/{id}/Personalizado/{subtype}/{suggestion}")
    public Optional<ArticleReview> findArticleReviewByIdAndCustomReplacement(
            @PathVariable("id") int articleId, @PathVariable("subtype") String subtype, @PathVariable("suggestion") String suggestion) {
        LOGGER.info("GET Find review for article by custom type. ID: {} - Subtype: {} - Suggestion: {}",
                articleId, subtype, suggestion);
        return articleService.findArticleReview(articleId, ReplacementFinderService.CUSTOM_FINDER_TYPE, subtype, suggestion);
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
        articleIndexService.reviewArticle(articleId, type, subtype, reviewer);
    }

    /* STATISTICS */

    @GetMapping(value = "/count/replacements")
    public Long countReplacements() {
        Long count = articleStatsService.countReplacements();
        LOGGER.info("GET Count replacements. Result: {}", count);
        return count;
    }

    @GetMapping(value = "/count/replacements/to-review")
    public Long countReplacementsToReview() {
        Long count = articleStatsService.countReplacementsToReview();
        LOGGER.info("GET Count not reviewed. Results: {}", count);
        return count;
    }

    @GetMapping("/count/replacements/reviewed")
    public Long countReplacementsReviewed() {
        Long count = articleStatsService.countReplacementsReviewed();
        LOGGER.info("GET Count reviewed replacements. Result: {}", count);
        return count;
    }

    @GetMapping(value = "/count/replacements/reviewed/grouped")
    public List<Object[]> countReplacementsGroupedByReviewer() {
        List<Object[]> list = articleStatsService.countReplacementsGroupedByReviewer();
        LOGGER.info("GET Count grouped by reviewer. Result Size: {}", list.size());
        return list;
    }

    /* LIST OF REPLACEMENTS */

    @GetMapping(value = "/count/replacements/grouped")
    public List<ReplacementCountList> listMisspellings() {
        List<ReplacementCountList> list = articleStatsService.findMisspellingsGrouped().asMap().entrySet().stream()
                .map(entry -> new ReplacementCountList(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        LOGGER.info("GET Grouped replacement count. Result Size: {}", list.size());
        Collections.sort(list);
        return list;
    }

}
