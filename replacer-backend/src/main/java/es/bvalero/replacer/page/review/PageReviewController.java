package es.bvalero.replacer.page.review;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.UserParameters;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "pages")
@Loggable(prepend = true, trim = false)
@RestController
@RequestMapping("api/pages")
public class PageReviewController {

    @Autowired
    private PageReviewNoTypeFinder pageReviewNoTypeFinder;

    @Autowired
    private PageReviewTypeSubtypeFinder pageReviewTypeSubtypeFinder;

    @Autowired
    private PageReviewCustomFinder pageReviewCustomFinder;

    /* FIND RANDOM PAGES WITH REPLACEMENTS */

    @ApiOperation(value = "Find a random page and the replacements to review", response = PageReviewResponse.class)
    @GetMapping(value = "/random")
    public Optional<PageReviewResponse> findRandomPageWithReplacements(@Valid PageReviewOptions options) {
        Optional<PageReview> review;
        if (StringUtils.isBlank(options.getType())) {
            review = pageReviewNoTypeFinder.findRandomPageReview(options);
        } else if (StringUtils.isBlank(options.getSuggestion())) {
            review = pageReviewTypeSubtypeFinder.findRandomPageReview(options);
        } else {
            review = pageReviewCustomFinder.findRandomPageReview(options);
        }
        return review.map(r -> PageReviewMapper.toDto(r, options));
    }

    @ApiOperation(value = "Validate if the custom replacement is a known subtype")
    @GetMapping(value = "/validate", params = { "replacement", "cs" })
    public MisspellingType validateCustomReplacement(
        @Valid UserParameters params,
        @ApiParam(value = "Replacement to validate", example = "aún") @RequestParam @Size(max = 100) String replacement,
        @ApiParam(value = "If the custom replacement is case-sensitive") @RequestParam(
            defaultValue = "false"
        ) boolean cs
    ) {
        return pageReviewCustomFinder.validateCustomReplacement(params.getLang(), replacement, cs);
    }

    /* FIND A PAGE REVIEW */

    @ApiOperation(value = "Find a page and the replacements to review", response = PageReviewResponse.class)
    @GetMapping(value = "/{id}")
    public Optional<PageReviewResponse> findPageReviewById(
        @ApiParam(value = "Page ID", example = "6980716") @PathVariable("id") int pageId,
        @Valid PageReviewOptions options
    ) {
        Optional<PageReview> review;
        if (StringUtils.isBlank(options.getType())) {
            review = pageReviewNoTypeFinder.getPageReview(pageId, options);
        } else if (StringUtils.isBlank(options.getSuggestion())) {
            review = pageReviewTypeSubtypeFinder.getPageReview(pageId, options);
        } else {
            review = pageReviewCustomFinder.getPageReview(pageId, options);
        }
        return review.map(r -> PageReviewMapper.toDto(r, options));
    }
}
