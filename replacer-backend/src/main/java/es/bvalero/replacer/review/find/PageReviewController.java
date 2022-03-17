package es.bvalero.replacer.review.find;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.dto.PageReviewOptionsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pages")
@Loggable(entered = true)
@RestController
@RequestMapping("api/pages")
public class PageReviewController {

    @Autowired
    private PageReviewNoTypeFinder pageReviewNoTypeFinder;

    @Autowired
    private PageReviewTypeFinder pageReviewTypeFinder;

    @Autowired
    private PageReviewCustomFinder pageReviewCustomFinder;

    @Operation(summary = "Find a random page and the replacements to review")
    @GetMapping(value = "/random")
    public Optional<PageReviewResponse> findRandomPageWithReplacements(
        @Valid CommonQueryParameters queryParameters,
        @Valid PageReviewOptionsDto optionsDto
    ) {
        Optional<PageReview> review = Optional.empty();
        PageReviewOptions options = PageReviewMapper.fromDto(optionsDto, false, queryParameters);
        switch (options.getOptionsType()) {
            case NO_TYPE:
                review = pageReviewNoTypeFinder.findRandomPageReview(options);
                break;
            case TYPE_SUBTYPE:
                review = pageReviewTypeFinder.findRandomPageReview(options);
                break;
            case CUSTOM:
                review = pageReviewCustomFinder.findRandomPageReview(options);
                break;
        }
        return review.map(r -> PageReviewMapper.toDto(r, options));
    }

    @Operation(summary = "Find a page and the replacements to review")
    @GetMapping(value = "/{id}")
    public Optional<PageReviewResponse> findPageReviewById(
        @Parameter(description = "Page ID", example = "6980716") @PathVariable("id") int pageId,
        @Valid CommonQueryParameters queryParameters,
        @Valid PageReviewOptionsDto optionsDto
    ) {
        Optional<PageReview> review = Optional.empty();
        PageReviewOptions options = PageReviewMapper.fromDto(optionsDto, false, queryParameters);
        switch (options.getOptionsType()) {
            case NO_TYPE:
                review = pageReviewNoTypeFinder.getPageReview(pageId, options);
                break;
            case TYPE_SUBTYPE:
                review = pageReviewTypeFinder.getPageReview(pageId, options);
                break;
            case CUSTOM:
                review = pageReviewCustomFinder.getPageReview(pageId, options);
                break;
        }
        return review.map(r -> PageReviewMapper.toDto(r, options));
    }
}
