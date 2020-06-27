package es.bvalero.replacer.page;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.authentication.AccessToken;
import es.bvalero.replacer.finder.CosmeticFindService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/pages")
public class PageController {
    @Autowired
    private PageReviewNoTypeService pageReviewNoTypeService;

    @Autowired
    private PageReviewTypeSubtypeService pageReviewTypeSubtypeService;

    @Autowired
    private PageReviewCustomService pageReviewCustomService;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private CosmeticFindService cosmeticFindService;

    /* FIND RANDOM ARTICLES WITH REPLACEMENTS */

    @GetMapping(value = "/random")
    public Optional<PageReview> findRandomArticleWithReplacements(@RequestParam WikipediaLanguage lang) {
        LOGGER.info("GET Find random page review");
        return pageReviewNoTypeService.findRandomPageReview(PageReviewOptions.ofNoType(lang));
    }

    @GetMapping(value = "/random", params = { "type", "subtype" })
    public Optional<PageReview> findRandomArticleByTypeAndSubtype(
        @RequestParam String type,
        @RequestParam String subtype,
        @RequestParam WikipediaLanguage lang
    ) {
        LOGGER.info("GET Find random page review. Type: {} - {}", type, subtype);
        return pageReviewTypeSubtypeService.findRandomPageReview(PageReviewOptions.ofTypeSubtype(lang, type, subtype));
    }

    @GetMapping(value = "/random", params = { "replacement", "suggestion" })
    public Optional<PageReview> findRandomArticleByCustomReplacement(
        @RequestParam String replacement,
        @RequestParam String suggestion,
        @RequestParam WikipediaLanguage lang
    ) {
        LOGGER.info("GET Find random page review. Custom replacement: {} - {}", replacement, suggestion);
        return pageReviewCustomService.findRandomPageReview(PageReviewOptions.ofCustom(lang, replacement, suggestion));
    }

    /* FIND AN ARTICLE REVIEW */

    @GetMapping(value = "/{id}")
    public Optional<PageReview> findPageReviewById(
        @PathVariable("id") int pageId,
        @RequestParam WikipediaLanguage lang
    ) {
        LOGGER.info("GET Find review by page ID: {}", pageId);
        return pageReviewNoTypeService.getPageReview(pageId, PageReviewOptions.ofNoType(lang));
    }

    @GetMapping(value = "/{id}", params = { "type", "subtype" })
    public Optional<PageReview> findPageReviewByIdByTypeAndSubtype(
        @PathVariable("id") int pageId,
        @RequestParam String type,
        @RequestParam String subtype,
        @RequestParam WikipediaLanguage lang
    ) {
        LOGGER.info("GET Find review by page ID: {} - Type: {} - Subtype: {}", pageId, type, subtype);
        return pageReviewTypeSubtypeService.getPageReview(pageId, PageReviewOptions.ofTypeSubtype(lang, type, subtype));
    }

    @GetMapping(value = "/{id}", params = { "replacement", "suggestion" })
    public Optional<PageReview> findPageReviewByIdAndCustomReplacement(
        @PathVariable("id") int pageId,
        @RequestParam String replacement,
        @RequestParam String suggestion,
        @RequestParam WikipediaLanguage lang
    ) {
        LOGGER.info(
            "GET Find review by page ID: {} - Replacement: {} - Suggestion: {}",
            pageId,
            replacement,
            suggestion
        );
        return pageReviewCustomService.getPageReview(pageId, PageReviewOptions.ofCustom(lang, replacement, suggestion));
    }

    /* SAVE CHANGES */

    @PostMapping(value = "/{id}")
    public void save(
        @RequestBody SavePage saveArticle,
        @PathVariable("id") int pageId,
        @RequestParam WikipediaLanguage lang
    )
        throws ReplacerException {
        boolean changed = StringUtils.isNotBlank(saveArticle.getContent());
        LOGGER.info("PUT Save page. ID: {} - Changed: {}", pageId, changed);
        if (changed) {
            // Upload new content to Wikipedia
            String textToSave = cosmeticFindService.applyCosmeticChanges(saveArticle.getContent());
            wikipediaService.savePageContent(
                pageId,
                textToSave,
                saveArticle.getSection(),
                saveArticle.getTimestamp(),
                lang,
                convertToEntity(saveArticle.getToken())
            );
        }

        // Mark article as reviewed in the database
        replacementIndexService.reviewArticleReplacements(
            pageId,
            lang,
            saveArticle.getType(),
            saveArticle.getSubtype(),
            saveArticle.getReviewer()
        );
    }

    private OAuth1AccessToken convertToEntity(AccessToken accessToken) {
        return new OAuth1AccessToken(accessToken.getToken(), accessToken.getTokenSecret());
    }
}
