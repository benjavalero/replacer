package es.bvalero.replacer.page.review;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "pages")
@Loggable(entered = true)
@RestController
@RequestMapping("api/pages")
public class PageReviewController {

    @Autowired
    private PageReviewNoTypeFinder pageReviewNoTypeFinder;

    @Autowired
    private PageReviewTypeSubtypeFinder pageReviewTypeSubtypeFinder;

    @Autowired
    private PageReviewCustomFinder pageReviewCustomFinder;

    @ApiOperation(value = "Find a random page and the replacements to review", response = PageReviewResponse.class)
    @GetMapping(value = "/random")
    public Optional<PageReviewResponse> findRandomPageWithReplacements(@Valid PageReviewOptions options) {
        Optional<PageReview> review = Optional.empty();
        switch (options.getOptionsType()) {
            case NO_TYPE:
                review = pageReviewNoTypeFinder.findRandomPageReview(options);
                break;
            case TYPE_SUBTYPE:
                review = pageReviewTypeSubtypeFinder.findRandomPageReview(options);
                break;
            case CUSTOM:
                review = pageReviewCustomFinder.findRandomPageReview(options);
                break;
        }
        return review.map(r -> PageReviewMapper.toDto(r, options));
    }

    @ApiOperation(value = "Find a page and the replacements to review", response = PageReviewResponse.class)
    @GetMapping(value = "/{id}")
    public Optional<PageReviewResponse> findPageReviewById(
        @ApiParam(value = "Page ID", example = "6980716") @PathVariable("id") int pageId,
        @Valid PageReviewOptions options
    ) {
        Optional<PageReview> review = Optional.empty();
        switch (options.getOptionsType()) {
            case NO_TYPE:
                review = pageReviewNoTypeFinder.getPageReview(pageId, options);
                break;
            case TYPE_SUBTYPE:
                review = pageReviewTypeSubtypeFinder.getPageReview(pageId, options);
                break;
            case CUSTOM:
                review = pageReviewCustomFinder.getPageReview(pageId, options);
                break;
        }
        return review.map(r -> PageReviewMapper.toDto(r, options));
    }

    @ApiOperation(value = "Validate if the custom replacement is a known subtype")
    @GetMapping(value = "/validate", params = { "replacement", "cs" })
    public ReplacementValidationResponse validateCustomReplacement(
        @Valid CommonQueryParameters queryParameters,
        @Valid ReplacementValidationRequest validationRequest
    ) {
        return pageReviewCustomFinder.validateCustomReplacement(
            queryParameters.getLang(),
            validationRequest.getReplacement(),
            validationRequest.isCs()
        );
    }
}
