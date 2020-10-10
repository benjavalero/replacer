package es.bvalero.replacer.page;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.authentication.AccessToken;
import es.bvalero.replacer.finder.CosmeticFindService;
import es.bvalero.replacer.replacement.ReplacementCountService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private PageListService pageListService;

    @Autowired
    private ReplacementCountService replacementCountService;

    /* FIND RANDOM PAGES WITH REPLACEMENTS */

    @GetMapping(value = "/random")
    public Optional<PageReview> findRandomPageWithReplacements(@RequestParam WikipediaLanguage lang) {
        LOGGER.info("GET Find random page review");
        return pageReviewNoTypeService.findRandomPageReview(PageReviewOptions.ofNoType(lang));
    }

    @GetMapping(value = "/random", params = { "type", "subtype" })
    public Optional<PageReview> findRandomPageByTypeAndSubtype(
        @RequestParam String type,
        @RequestParam String subtype,
        @RequestParam WikipediaLanguage lang
    ) {
        LOGGER.info("GET Find random page review. Type: {} - {}", type, subtype);
        return pageReviewTypeSubtypeService.findRandomPageReview(PageReviewOptions.ofTypeSubtype(lang, type, subtype));
    }

    @GetMapping(value = "/random", params = { "replacement", "suggestion" })
    public Optional<PageReview> findRandomPageByCustomReplacement(
        @RequestParam String replacement,
        @RequestParam String suggestion,
        @RequestParam WikipediaLanguage lang
    ) {
        LOGGER.info("GET Find random page review. Custom replacement: {} - {}", replacement, suggestion);
        return pageReviewCustomService.findRandomPageReview(PageReviewOptions.ofCustom(lang, replacement, suggestion));
    }

    /* FIND A PAGE REVIEW */

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
        @RequestBody SavePage savePage,
        @PathVariable("id") int pageId,
        @RequestParam WikipediaLanguage lang
    )
        throws ReplacerException {
        boolean changed = StringUtils.isNotBlank(savePage.getContent());
        LOGGER.info("POST Save page. ID: {} - Changed: {}", pageId, changed);
        if (changed) {
            // Upload new content to Wikipedia
            String textToSave = cosmeticFindService.applyCosmeticChanges(savePage.getContent());
            wikipediaService.savePageContent(
                pageId,
                textToSave,
                savePage.getSection(),
                savePage.getTimestamp(),
                lang,
                convertToEntity(savePage.getToken())
            );
        }

        // Mark page as reviewed in the database
        replacementIndexService.reviewPageReplacements(
            pageId,
            lang,
            savePage.getType(),
            savePage.getSubtype(),
            savePage.getReviewer()
        );
    }

    /* PAGE LIST FOR ROBOTS */

    @GetMapping(value = "/list", params = { "type", "subtype" }, produces = "text/plain")
    public ResponseEntity<String> findPageListByTypeAndSubtype(
        @RequestParam String type,
        @RequestParam String subtype,
        @RequestParam WikipediaLanguage lang
    ) {
        LOGGER.info("GET Find page list. Type: {} - {}", type, subtype);
        String titleList = StringUtils.join(pageListService.findPageList(lang, type, subtype), "\n");
        return new ResponseEntity<>(titleList, HttpStatus.OK);
    }

    @PostMapping(value = "/review", params = { "type", "subtype" })
    public void reviewPagesByTypeAndSubtype(WikipediaLanguage lang, String type, String subtype) {
        LOGGER.info("POST Review pages. Type: {} - {}", type, subtype);

        // Set as reviewed in the database
        pageListService.reviewPagesByTypeAndSubtype(lang, type, subtype);

        // Remove from the replacement count cache
        replacementCountService.removeCachedReplacementCount(lang, type, subtype);
    }

    private OAuth1AccessToken convertToEntity(AccessToken accessToken) {
        return new OAuth1AccessToken(accessToken.getToken(), accessToken.getTokenSecret());
    }
}
