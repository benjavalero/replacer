package es.bvalero.replacer.page;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.UserParameters;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import es.bvalero.replacer.page.review.PageReviewMapper;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.page.review.PageReviewSearch;
import es.bvalero.replacer.page.review.ReviewSection;
import es.bvalero.replacer.replacement.CustomEntity;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import es.bvalero.replacer.wikipedia.WikipediaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.time.LocalDate;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private ReplacementService replacementService;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private CosmeticFinderService cosmeticFinderService;

    /* SAVE CHANGES */

    @ApiOperation(value = "Update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    public ResponseEntity<String> save(
        @PathVariable("id") int pageId,
        @Valid UserParameters params,
        @ApiParam(value = "Page to update and mark as reviewed") @Valid @RequestBody SavePage savePage
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
                ReviewSection section = savePage.getPage().getSection();
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
        PageReviewOptions options = PageReviewMapper.fromDto(search, lang);
        switch (options.getOptionsType()) {
            case NO_TYPE:
                markAsReviewedNoType(pageId, options, reviewer);
                break;
            case TYPE_SUBTYPE:
                markAsReviewedTypeSubtype(pageId, options, reviewer);
                break;
            case CUSTOM:
                markAsReviewedCustom(pageId, options, reviewer);
                break;
        }
    }

    private void markAsReviewedNoType(int pageId, PageReviewOptions options, String reviewer) {
        replacementService.reviewByPageId(options.getLang(), pageId, null, null, reviewer);
    }

    private void markAsReviewedTypeSubtype(int pageId, PageReviewOptions options, String reviewer) {
        replacementService.reviewByPageId(options.getLang(), pageId, options.getType(), options.getSubtype(), reviewer);
    }

    private void markAsReviewedCustom(int pageId, PageReviewOptions options, String reviewer) {
        // Custom replacements don't exist in the database to be reviewed
        String subtype = options.getSubtype();
        boolean cs = options.getCs() != null && Boolean.TRUE.equals(options.getCs());
        assert subtype != null;
        replacementService.insert(buildCustomReviewed(pageId, options.getLang(), subtype, cs, reviewer));
    }

    private CustomEntity buildCustomReviewed(
        int pageId,
        WikipediaLanguage lang,
        String replacement,
        boolean cs,
        String reviewer
    ) {
        return CustomEntity
            .builder()
            .lang(lang.getCode())
            .pageId(pageId)
            .replacement(replacement)
            .cs(cs)
            .lastUpdate(LocalDate.now())
            .reviewer(reviewer)
            .build();
    }
}
