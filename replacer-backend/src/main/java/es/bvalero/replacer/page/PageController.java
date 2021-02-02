package es.bvalero.replacer.page;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.UserParameters;
import es.bvalero.replacer.finder.CosmeticFindService;
import es.bvalero.replacer.replacement.ReplacementCountService;
import es.bvalero.replacer.replacement.ReplacementEntity;
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

@Loggable(prepend = true, trim = false)
@RestController
@RequestMapping("api/pages")
public class PageController {

    static final int CONTENT_SIZE = 50;

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
    public Optional<PageReview> findRandomPageWithReplacements(UserParameters params) {
        return pageReviewNoTypeService.findRandomPageReview(PageReviewOptions.ofNoType(params.getLang()));
    }

    @GetMapping(value = "/random", params = { "type", "subtype" })
    public Optional<PageReview> findRandomPageByTypeAndSubtype(
        @RequestParam String type,
        @RequestParam String subtype,
        UserParameters params
    ) {
        return pageReviewTypeSubtypeService.findRandomPageReview(
            PageReviewOptions.ofTypeSubtype(params.getLang(), type, subtype)
        );
    }

    @GetMapping(value = "/random", params = { "replacement", "suggestion" })
    public Optional<PageReview> findRandomPageByCustomReplacement(
        @RequestParam @Size(max = 100) String replacement,
        @RequestParam String suggestion,
        UserParameters params
    ) {
        return pageReviewCustomService.findRandomPageReview(
            PageReviewOptions.ofCustom(params.getLang(), replacement, suggestion)
        );
    }

    @GetMapping(value = "/validate", params = { "replacement" })
    public Optional<String> validateCustomReplacement(
        @RequestParam @Size(max = 100) String replacement,
        UserParameters params
    ) {
        return pageReviewCustomService.validateCustomReplacement(replacement, params.getLang());
    }

    /* FIND A PAGE REVIEW */

    @GetMapping(value = "/{id}")
    public Optional<PageReview> findPageReviewById(@PathVariable("id") int pageId, UserParameters params) {
        return pageReviewNoTypeService.getPageReview(pageId, PageReviewOptions.ofNoType(params.getLang()));
    }

    @GetMapping(value = "/{id}", params = { "type", "subtype" })
    public Optional<PageReview> findPageReviewByIdByTypeAndSubtype(
        @PathVariable("id") int pageId,
        @RequestParam String type,
        @RequestParam String subtype,
        UserParameters params
    ) {
        return pageReviewTypeSubtypeService.getPageReview(
            pageId,
            PageReviewOptions.ofTypeSubtype(params.getLang(), type, subtype)
        );
    }

    @GetMapping(value = "/{id}", params = { "replacement", "suggestion" })
    public Optional<PageReview> findPageReviewByIdAndCustomReplacement(
        @PathVariable("id") int pageId,
        @RequestParam @Size(max = 100) String replacement,
        @RequestParam String suggestion,
        UserParameters params
    ) {
        return pageReviewCustomService.getPageReview(
            pageId,
            PageReviewOptions.ofCustom(params.getLang(), replacement, suggestion)
        );
    }

    /* SAVE CHANGES */

    @PostMapping(value = "/{id}")
    public ResponseEntity<String> save(
        @RequestBody SavePage savePage,
        @PathVariable("id") int pageId,
        UserParameters params
    ) {
        boolean changed = StringUtils.isNotBlank(savePage.getContent());
        if (changed) {
            // Upload new content to Wikipedia
            try {
                // Apply cosmetic changes
                WikipediaPage page = WikipediaPage
                    .builder()
                    .lang(params.getLang())
                    .content(savePage.getContent())
                    .title(savePage.getTitle())
                    .build();
                String textToSave = cosmeticFindService.applyCosmeticChanges(page);
                wikipediaService.savePageContent(
                    params.getLang(),
                    pageId,
                    savePage.getSection(),
                    textToSave,
                    savePage.getTimestamp(),
                    savePage.getAccessToken()
                );
            } catch (ReplacerException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        // Mark page as reviewed in the database
        if (ReplacementEntity.TYPE_CUSTOM.equals(savePage.getType())) {
            if (StringUtils.isBlank(savePage.getSubtype())) throw new AssertionError();
            pageReviewCustomService.reviewPageReplacements( // NOSONAR
                pageId,
                params.getLang(),
                savePage.getSubtype(),
                params.getUser()
            );
        } else if (StringUtils.isNotBlank(savePage.getType())) {
            if (StringUtils.isBlank(savePage.getSubtype())) throw new AssertionError();
            pageReviewTypeSubtypeService.reviewPageReplacements( // NOSONAR
                pageId,
                params.getLang(),
                savePage.getType(),
                savePage.getSubtype(),
                params.getUser()
            );
        } else {
            if (StringUtils.isNotBlank(savePage.getSubtype())) throw new AssertionError();
            pageReviewNoTypeService.reviewPageReplacements(pageId, params.getLang(), params.getUser());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /* PAGE LIST FOR ROBOTS */

    @GetMapping(value = "/list", params = { "type", "subtype" }, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> findPageTitlesToReviewBySubtype(
        @RequestParam String type,
        @RequestParam String subtype,
        UserParameters params
    ) {
        String titleList = StringUtils.join(
            pageListService.findPageTitlesToReviewBySubtype(params.getLang(), type, subtype),
            "\n"
        );
        return new ResponseEntity<>(titleList, HttpStatus.OK);
    }

    @PostMapping(value = "/review", params = { "type", "subtype" })
    public void reviewAsSystemBySubtype(UserParameters params, String type, String subtype) {
        // Set as reviewed in the database
        pageListService.reviewAsSystemBySubtype(params.getLang(), type, subtype);

        // Remove from the replacement count cache
        replacementCountService.removeCachedReplacementCount(params.getLang(), type, subtype);
    }
}
