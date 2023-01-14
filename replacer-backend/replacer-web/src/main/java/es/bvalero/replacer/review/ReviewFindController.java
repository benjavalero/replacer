package es.bvalero.replacer.review;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.page.PageKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review")
@Loggable(entered = true)
@RestController
@RequestMapping("api/review")
public class ReviewFindController {

    @Autowired
    private ReviewNoTypeFinder reviewNoTypeFinder;

    @Autowired
    private ReviewTypeFinder reviewTypeFinder;

    @Autowired
    private ReviewCustomFinder reviewCustomFinder;

    @Operation(summary = "Find a random page and the replacements to review")
    @GetMapping(value = "/random")
    public Optional<FindReviewResponse> findRandomPageWithReplacements(
        @Valid CommonQueryParameters queryParameters,
        @Valid ReviewOptionsDto optionsDto
    ) {
        Optional<Review> review = Optional.empty();
        ReviewOptions options = ReviewMapper.fromDto(optionsDto, queryParameters);
        switch (options.getOptionsType()) {
            case NO_TYPE:
                review = reviewNoTypeFinder.findRandomPageReview(options);
                break;
            case TYPE_SUBTYPE:
                review = reviewTypeFinder.findRandomPageReview(options);
                break;
            case CUSTOM:
                review = reviewCustomFinder.findRandomPageReview(options);
                break;
        }
        return review.map(r -> ReviewMapper.toDto(r, options));
    }

    @Operation(summary = "Find a page and the replacements to review")
    @GetMapping(value = "/{id}")
    public Optional<FindReviewResponse> findPageReviewById(
        @Parameter(description = "Page ID", example = "6980716") @PathVariable("id") int pageId,
        @Valid CommonQueryParameters queryParameters,
        @Valid ReviewOptionsDto optionsDto
    ) {
        Optional<Review> review = Optional.empty();
        ReviewOptions options = ReviewMapper.fromDto(optionsDto, queryParameters);
        PageKey pageKey = PageKey.of(queryParameters.getLang(), pageId);
        switch (options.getOptionsType()) {
            case NO_TYPE:
                review = reviewNoTypeFinder.findPageReview(pageKey, options);
                break;
            case TYPE_SUBTYPE:
                review = reviewTypeFinder.findPageReview(pageKey, options);
                break;
            case CUSTOM:
                review = reviewCustomFinder.findPageReview(pageKey, options);
                break;
        }
        return review.map(r -> ReviewMapper.toDto(r, options));
    }
}
