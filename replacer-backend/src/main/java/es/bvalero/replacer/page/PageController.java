package es.bvalero.replacer.page;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.authentication.AccessToken;
import es.bvalero.replacer.finder.CosmeticFindService;
import es.bvalero.replacer.replacement.ReplacementCountService;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Optional;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Loggable(prepend = true)
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
        return pageReviewNoTypeService.findRandomPageReview(PageReviewOptions.ofNoType(lang));
    }

    @GetMapping(value = "/random", params = { "type", "subtype" })
    public Optional<PageReview> findRandomPageByTypeAndSubtype(
        @RequestParam String type,
        @RequestParam String subtype,
        @RequestParam WikipediaLanguage lang
    ) {
        return pageReviewTypeSubtypeService.findRandomPageReview(PageReviewOptions.ofTypeSubtype(lang, type, subtype));
    }

    @GetMapping(value = "/random", params = { "replacement", "suggestion" })
    public Optional<PageReview> findRandomPageByCustomReplacement(
        @RequestParam @Size(max = 100) String replacement,
        @RequestParam String suggestion,
        @RequestParam WikipediaLanguage lang
    ) {
        return pageReviewCustomService.findRandomPageReview(PageReviewOptions.ofCustom(lang, replacement, suggestion));
    }

    @GetMapping(value = "/validate", params = { "replacement" })
    public Optional<String> validateCustomReplacement(
        @RequestParam @Size(max = 100) String replacement,
        @RequestParam WikipediaLanguage lang
    ) {
        return pageReviewCustomService.validateCustomReplacement(replacement, lang);
    }

    /* FIND A PAGE REVIEW */

    @GetMapping(value = "/{id}")
    public Optional<PageReview> findPageReviewById(
        @PathVariable("id") int pageId,
        @RequestParam WikipediaLanguage lang
    ) {
        return pageReviewNoTypeService.getPageReview(pageId, PageReviewOptions.ofNoType(lang));
    }

    @GetMapping(value = "/{id}", params = { "type", "subtype" })
    public Optional<PageReview> findPageReviewByIdByTypeAndSubtype(
        @PathVariable("id") int pageId,
        @RequestParam String type,
        @RequestParam String subtype,
        @RequestParam WikipediaLanguage lang
    ) {
        return pageReviewTypeSubtypeService.getPageReview(pageId, PageReviewOptions.ofTypeSubtype(lang, type, subtype));
    }

    @GetMapping(value = "/{id}", params = { "replacement", "suggestion" })
    public Optional<PageReview> findPageReviewByIdAndCustomReplacement(
        @PathVariable("id") int pageId,
        @RequestParam @Size(max = 100) String replacement,
        @RequestParam String suggestion,
        @RequestParam WikipediaLanguage lang
    ) {
        return pageReviewCustomService.getPageReview(pageId, PageReviewOptions.ofCustom(lang, replacement, suggestion));
    }

    /* SAVE CHANGES */

    @Loggable(prepend = true)
    @PostMapping(value = "/{id}")
    public ResponseEntity<String> save(
        @RequestBody SavePage savePage,
        @PathVariable("id") int pageId,
        @RequestParam WikipediaLanguage lang
    ) {
        boolean changed = StringUtils.isNotBlank(savePage.getContent());
        if (changed) {
            // Upload new content to Wikipedia
            try {
                // Apply cosmetic changes
                WikipediaPage page = WikipediaPage
                    .builder()
                    .lang(lang)
                    .content(savePage.getContent())
                    .title(savePage.getTitle())
                    .build();
                String textToSave = cosmeticFindService.applyCosmeticChanges(page);
                wikipediaService.savePageContent(
                    lang,
                    pageId,
                    savePage.getSection(),
                    textToSave,
                    savePage.getTimestamp(),
                    convertToEntity(savePage.getToken())
                );
            } catch (ReplacerException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        // Mark page as reviewed in the database
        if (ReplacementEntity.TYPE_CUSTOM.equals(savePage.getType())) {
            if (StringUtils.isBlank(savePage.getSubtype())) throw new AssertionError();
            pageReviewCustomService.reviewPageReplacements(pageId, lang, savePage.getSubtype(), savePage.getReviewer()); // NOSONAR
        } else if (StringUtils.isNotBlank(savePage.getType())) {
            if (StringUtils.isBlank(savePage.getSubtype())) throw new AssertionError();
            pageReviewTypeSubtypeService.reviewPageReplacements( // NOSONAR
                pageId,
                lang,
                savePage.getType(),
                savePage.getSubtype(),
                savePage.getReviewer()
            );
        } else {
            if (StringUtils.isNotBlank(savePage.getSubtype())) throw new AssertionError();
            pageReviewNoTypeService.reviewPageReplacements(pageId, lang, savePage.getReviewer());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /* PAGE LIST FOR ROBOTS */

    @GetMapping(value = "/list", params = { "type", "subtype" }, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> findPageTitlesToReviewBySubtype(
        @RequestParam String type,
        @RequestParam String subtype,
        @RequestParam WikipediaLanguage lang
    ) {
        String titleList = StringUtils.join(pageListService.findPageTitlesToReviewBySubtype(lang, type, subtype), "\n");
        return new ResponseEntity<>(titleList, HttpStatus.OK);
    }

    @PostMapping(value = "/review", params = { "type", "subtype" })
    public void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype) {
        // Set as reviewed in the database
        pageListService.reviewAsSystemBySubtype(lang, type, subtype);

        // Remove from the replacement count cache
        replacementCountService.removeCachedReplacementCount(lang, type, subtype);
    }

    private OAuth1AccessToken convertToEntity(AccessToken accessToken) {
        return new OAuth1AccessToken(accessToken.getToken(), accessToken.getTokenSecret());
    }
}
