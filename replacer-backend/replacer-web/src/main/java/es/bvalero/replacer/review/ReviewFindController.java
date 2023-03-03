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
        Optional<Review> review;
        ReviewOptions options = ReviewMapper.fromDto(optionsDto, queryParameters);
        if (options.getType().isNoType()) {
            review = reviewNoTypeFinder.findRandomPageReview(options);
        } else if (options.getType().isStandardType()) {
            review = reviewTypeFinder.findRandomPageReview(options);
        } else if (options.getType().isCustomType()) {
            review = reviewCustomFinder.findRandomPageReview(options);
        } else {
            throw new IllegalStateException();
        }
        return review.map(ReviewMapper::toDto);
    }

    @Operation(summary = "Find a page and the replacements to review")
    @GetMapping(value = "/{id}")
    public Optional<FindReviewResponse> findPageReviewById(
        @Parameter(description = "Page ID", example = "6980716") @PathVariable("id") int pageId,
        @Valid CommonQueryParameters queryParameters,
        @Valid ReviewOptionsDto optionsDto
    ) {
        Optional<Review> review;
        ReviewOptions options = ReviewMapper.fromDto(optionsDto, queryParameters);
        PageKey pageKey = PageKey.of(queryParameters.getWikipediaLanguage(), pageId);
        if (options.getType().isNoType()) {
            review = reviewNoTypeFinder.findPageReview(pageKey, options);
        } else if (options.getType().isStandardType()) {
            review = reviewTypeFinder.findPageReview(pageKey, options);
        } else if (options.getType().isCustomType()) {
            review = reviewCustomFinder.findPageReview(pageKey, options);
        } else {
            throw new IllegalStateException();
        }
        return review.map(ReviewMapper::toDto);
    }
}
