package es.bvalero.replacer.page.review;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.UserParameters;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Optional;
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
    private PageReviewNoTypeService pageReviewNoTypeService;

    @Autowired
    private PageReviewTypeSubtypeService pageReviewTypeSubtypeService;

    @Autowired
    private PageReviewCustomService pageReviewCustomService;

    /* FIND RANDOM PAGES WITH REPLACEMENTS */

    @ApiOperation(value = "Find a random page and the replacements to review", response = PageReview.class)
    @GetMapping(value = "/random")
    public Optional<PageReviewDto> findRandomPageWithReplacements(PageReviewOptions options) {
        Optional<PageReview> review;
        if (StringUtils.isBlank(options.getType())) {
            review = pageReviewNoTypeService.findRandomPageReview(options);
        } else if (StringUtils.isBlank(options.getSuggestion())) {
            review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        } else {
            review = pageReviewCustomService.findRandomPageReview(options);
        }
        return review.map(PageReviewMapper::toDto);
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
    public Optional<PageReviewDto> findPageReviewById(
        @ApiParam(value = "Page ID", example = "6980716") @PathVariable("id") int pageId,
        PageReviewOptions options
    ) {
        Optional<PageReview> review;
        if (StringUtils.isBlank(options.getType())) {
            review = pageReviewNoTypeService.getPageReview(pageId, options);
        } else if (StringUtils.isBlank(options.getSuggestion())) {
            review = pageReviewTypeSubtypeService.getPageReview(pageId, options);
        } else {
            review = pageReviewCustomService.getPageReview(pageId, options);
        }
        return review.map(PageReviewMapper::toDto);
    }
}
