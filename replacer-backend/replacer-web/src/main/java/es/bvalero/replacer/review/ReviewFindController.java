package es.bvalero.replacer.review;

import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AuthenticatedUser;
import es.bvalero.replacer.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Page")
@Slf4j
@RestController
@RequestMapping("api/page")
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
        @AuthenticatedUser User user,
        @Valid ReviewOptionsDto optionsDto
    ) {
        LOGGER.info("START Find Random Page with Replacements. Options: {}", optionsDto);
        Optional<Review> review;
        ReviewOptions options = ReviewMapper.fromDto(optionsDto, user);
        switch (options.getKind()) {
            case EMPTY:
                review = reviewNoTypeFinder.findRandomPageReview(options);
                break;
            case CUSTOM:
                review = reviewCustomFinder.findRandomPageReview(options);
                break;
            default:
                review = reviewTypeFinder.findRandomPageReview(options);
        }
        Optional<FindReviewResponse> response = review.map(ReviewMapper::toDto);
        LOGGER.info("END Find Random Page with Replacements: {}", response.isPresent() ? response.get() : "No Review");
        return response;
    }

    @Operation(summary = "Find a page and the replacements to review")
    @GetMapping(value = "/{id}")
    public Optional<FindReviewResponse> findPageReviewById(
        @Parameter(description = "Page ID", example = "6980716") @PathVariable("id") int pageId,
        @AuthenticatedUser User user,
        @Valid ReviewOptionsDto optionsDto
    ) {
        LOGGER.info("START Find Page Review by ID {}. Options: {}", pageId, optionsDto);
        Optional<Review> review;
        ReviewOptions options = ReviewMapper.fromDto(optionsDto, user);
        PageKey pageKey = PageKey.of(user.getId().getLang(), pageId);
        switch (options.getKind()) {
            case EMPTY:
                review = reviewNoTypeFinder.findPageReview(pageKey, options);
                break;
            case CUSTOM:
                review = reviewCustomFinder.findPageReview(pageKey, options);
                break;
            default:
                review = reviewTypeFinder.findPageReview(pageKey, options);
        }
        Optional<FindReviewResponse> response = review.map(ReviewMapper::toDto);
        LOGGER.info("END Find Random Page with Replacements: {}", response.isPresent() ? response.get() : "No Review");
        return response;
    }
}
