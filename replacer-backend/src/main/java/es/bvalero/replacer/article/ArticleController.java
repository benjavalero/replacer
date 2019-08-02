package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("api/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleStatsService articleStatsService;

    /* FIND RANDOM ARTICLES WITH REPLACEMENTS */

    @GetMapping(value = "/random")
    public List<Integer> findRandomArticleWithReplacements() {
        LOGGER.info("GET Find random article with replacements");
        return articleService.findRandomArticleToReview(null, null)
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    @GetMapping(value = "/random/{type}/{subtype}")
    public List<Integer> findRandomArticleByTypeAndSubtype(
            @PathVariable("type") String type, @PathVariable("subtype") String subtype) {
        LOGGER.info("GET Find random article with replacements. Type: {} - Subtype: {}", type, subtype);
        Optional<Integer> randomId;
        if (ReplacementFinderService.CUSTOM_FINDER_TYPE.equals(type)) {
            randomId = articleService.findRandomArticleToReviewByCustomReplacement(subtype);
        } else {
            randomId = articleService.findRandomArticleToReview(type, subtype);
        }
        return randomId.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    /* FIND AN ARTICLE REVIEW */

    @GetMapping(value = "/{id}")
    public Optional<ArticleReview> findArticleReviewById(@PathVariable("id") int articleId) {
        LOGGER.info("GET Find replacements for article. ID: {}", articleId);
        return articleService.findArticleReviewById(articleId, null, null);
    }

    @GetMapping(value = "/{id}/{type}/{subtype}")
    public Optional<ArticleReview> findArticleReviewByIdByTypeAndSubtype(
            @PathVariable("id") int articleId, @PathVariable("type") String type, @PathVariable("subtype") String subtype) {
        LOGGER.info("GET Find replacements for article. ID: {} - Type: {} - Subtype: {}", articleId, type, subtype);
        return articleService.findArticleReviewById(articleId, type, subtype);
    }

    @GetMapping(value = "/{id}/Personalizado/{subtype}/{suggestion}")
    public Optional<ArticleReview> findArticleReviewByIdAndCustomReplacement(
            @PathVariable("id") int articleId, @PathVariable("subtype") String subtype, @PathVariable("suggestion") String suggestion) {
        LOGGER.info("GET Find replacements for article by custom type. ID: {} - Subtype: {} - Suggestion: {}",
                articleId, subtype, suggestion);
        return articleService.findArticleReviewByIdAndCustomReplacement(articleId, subtype, suggestion);
    }

    /* SAVE CHANGES */

    @PutMapping
    public void save(@RequestParam("id") int articleId, @RequestBody String text,
                     @RequestParam String type, @RequestParam String subtype,
                     @RequestParam String reviewer, @RequestParam String currentTimestamp,
                     @RequestParam String token, @RequestParam String tokenSecret) throws WikipediaException {
        boolean changed = StringUtils.isNotBlank(text);
        LOGGER.info("PUT Save article. ID: {} - Changed: {}", articleId, changed);
        if (changed) {
            OAuth1AccessToken accessToken = new OAuth1AccessToken(token, tokenSecret);
            articleService.saveArticleChanges(articleId, text, type, subtype, reviewer, currentTimestamp, accessToken);
        } else {
            articleService.markArticleAsReviewed(articleId, type, subtype, reviewer);
        }
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
    public List<ReplacementCount> listMisspellings() {
        List<ReplacementCount> list = articleStatsService.findMisspellingsGrouped();
        LOGGER.info("GET Grouped replacement count. Result Size: {}", list.size());
        return list;
    }

}
