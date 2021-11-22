package es.bvalero.replacer.page;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.UserParameters;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import es.bvalero.replacer.wikipedia.WikipediaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Optional;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "pages")
@Loggable(prepend = true, trim = false)
@RestController
@RequestMapping("api/pages")
public class PageController {

    private static final String EDIT_SUMMARY = "Reemplazos con [[Usuario:Benjavalero/Replacer|Replacer]]";
    private static final String COSMETIC_CHANGES = "mejoras cosméticas";

    @Autowired
    private PageReviewNoTypeService pageReviewNoTypeService;

    @Autowired
    private PageReviewTypeSubtypeService pageReviewTypeSubtypeService;

    @Autowired
    private PageReviewCustomService pageReviewCustomService;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private CosmeticFinderService cosmeticFinderService;

    @Autowired
    private PageListService pageListService;

    /* FIND RANDOM PAGES WITH REPLACEMENTS */

    @ApiOperation(value = "Find a random page and the replacements to review", response = PageReview.class)
    @GetMapping(value = "/random")
    public Optional<PageReview> findRandomPageWithReplacements(PageReviewOptions options) {
        if (StringUtils.isBlank(options.getType())) {
            return pageReviewNoTypeService.findRandomPageReview(options);
        } else if (StringUtils.isBlank(options.getSuggestion())) {
            return pageReviewTypeSubtypeService.findRandomPageReview(options);
        } else {
            return pageReviewCustomService.findRandomPageReview(options);
        }
    }

    @ApiOperation(value = "Validate if the custom replacement is a known subtype")
    @GetMapping(value = "/validate", params = { "replacement", "cs" })
    public MisspellingType validateCustomReplacement(
        UserParameters params,
        @ApiParam(value = "Replacement to validate", example = "aún") @RequestParam @Size(max = 100) String replacement,
        @ApiParam(value = "If the custom replacement is case-sensitive") @RequestParam(
            defaultValue = "false"
        ) boolean cs
    ) {
        return pageReviewCustomService.validateCustomReplacement(params.getLang(), replacement, cs);
    }

    /* FIND A PAGE REVIEW */

    @ApiOperation(value = "Find a page and the replacements to review", response = PageReview.class)
    @GetMapping(value = "/{id}")
    public Optional<PageReview> findPageReviewById(
        @ApiParam(value = "Page ID", example = "6980716") @PathVariable("id") int pageId,
        PageReviewOptions options
    ) {
        if (StringUtils.isBlank(options.getType())) {
            return pageReviewNoTypeService.getPageReview(pageId, options);
        } else if (StringUtils.isBlank(options.getSuggestion())) {
            return pageReviewTypeSubtypeService.getPageReview(pageId, options);
        } else {
            return pageReviewCustomService.getPageReview(pageId, options);
        }
    }

    /* SAVE CHANGES */

    @ApiOperation(value = "Update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    public ResponseEntity<String> save(
        @PathVariable("id") int pageId,
        UserParameters params,
        @ApiParam(value = "Page to update and mark as reviewed") @RequestBody SavePage savePage
    ) {
        if (!savePage.getPage().getLang().equals(params.getLang()) || savePage.getPage().getId() != pageId) {
            throw new IllegalArgumentException();
        }

        boolean changed = StringUtils.isNotBlank(savePage.getPage().getContent());
        if (changed) {
            // Upload new content to Wikipedia
            try {
                // Apply cosmetic changes
                FinderPage page = FinderPage.of(
                    params.getLang(),
                    savePage.getPage().getContent(),
                    savePage.getPage().getTitle()
                );
                String textToSave = cosmeticFinderService.applyCosmeticChanges(page);
                boolean applyCosmetics = !textToSave.equals(page.getContent());
                PageSection section = savePage.getPage().getSection();
                wikipediaService.savePageContent(
                    WikipediaPageId.of(params.getLang(), pageId),
                    section == null ? null : section.getId(),
                    textToSave,
                    WikipediaDateUtils.parseWikipediaTimestamp(savePage.getPage().getQueryTimestamp()),
                    buildEditSummary(savePage.getSearch(), applyCosmetics),
                    AccessToken.of(savePage.getToken(), savePage.getTokenSecret())
                );
            } catch (ReplacerException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        // Mark page as reviewed in the database
        this.markAsReviewed(pageId, params.getLang(), params.getUser(), savePage.getSearch());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String buildEditSummary(PageReviewSearch search, boolean applyCosmetics) {
        StringBuilder summary = new StringBuilder(EDIT_SUMMARY);
        if (StringUtils.isNotBlank(search.getType()) && StringUtils.isNotBlank(search.getSubtype())) {
            summary.append(": «").append(search.getSubtype()).append('»');
        }
        if (applyCosmetics) {
            summary.append(" + ").append(COSMETIC_CHANGES);
        }
        return summary.toString();
    }

    private void markAsReviewed(int pageId, WikipediaLanguage lang, String reviewer, PageReviewSearch search) {
        if (ReplacementType.CUSTOM.getLabel().equals(search.getType())) {
            pageReviewCustomService.reviewPageReplacements(pageId, convert(search, lang), reviewer);
        } else if (StringUtils.isNotBlank(search.getType())) {
            pageReviewTypeSubtypeService.reviewPageReplacements(pageId, convert(search, lang), reviewer);
        } else {
            pageReviewNoTypeService.reviewPageReplacements(pageId, convert(search, lang), reviewer);
        }
    }

    private PageReviewOptions convert(PageReviewSearch search, WikipediaLanguage lang) {
        return PageReviewOptions
            .builder()
            .lang(lang)
            .type(search.getType())
            .subtype(search.getSubtype())
            .suggestion(search.getSuggestion())
            .cs(search.getCs())
            .build();
    }

    /* PAGE LIST FOR ROBOTS */

    @ApiOperation(
        value = "Produce a list in plain text with the titles of the pages containing the given replacement type to review"
    )
    @GetMapping(value = "", params = { "type", "subtype" }, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> findPageTitlesToReviewBySubtype(
        UserParameters params,
        @ApiParam(value = "Replacement type", example = "Ortografía") @RequestParam String type,
        @ApiParam(value = "Replacement subtype", example = "aún") @RequestParam String subtype
    ) {
        String titleList = StringUtils.join(
            pageListService.findPageTitlesToReviewBySubtype(params.getLang(), type, subtype),
            "\n"
        );
        return new ResponseEntity<>(titleList, HttpStatus.OK);
    }

    @ApiOperation(value = "Mark as reviewed by the system all pages pages containing the given replacement type")
    @PostMapping(value = "/review", params = { "type", "subtype" })
    public void reviewAsSystemBySubtype(
        UserParameters params,
        @ApiParam(value = "Replacement type", example = "Ortografía") @RequestParam String type,
        @ApiParam(value = "Replacement subtype", example = "aún") @RequestParam String subtype
    ) {
        // Set as reviewed in the database
        pageListService.reviewAsSystemBySubtype(params.getLang(), type, subtype);
    }
}
