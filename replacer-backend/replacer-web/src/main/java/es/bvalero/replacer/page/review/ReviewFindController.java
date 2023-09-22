package es.bvalero.replacer.page.review;

import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AuthenticatedUser;
import es.bvalero.replacer.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Page")
@Slf4j
@RestController
@RequestMapping("api/page")
public class ReviewFindController {

    static final String TOTAL_PAGES_HEADER = "X-Pagination-Total-Pages";

    @Autowired
    private ReviewNoTypeFinder reviewNoTypeFinder;

    @Autowired
    private ReviewTypeFinder reviewTypeFinder;

    @Autowired
    private ReviewCustomFinder reviewCustomFinder;

    @Operation(summary = "Find a random page and the replacements to review")
    @GetMapping(value = "/random")
    public ResponseEntity<Page> findRandomPageWithReplacements(
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
        return buildResponse(review);
    }

    @Operation(summary = "Find a page and the replacements to review")
    @GetMapping(value = "/{id}")
    public ResponseEntity<Page> findPageReviewById(
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
        return buildResponse(review);
    }

    private ResponseEntity<Page> buildResponse(Optional<Review> review) {
        if (review.isPresent()) {
            Page response = ReviewMapper.toDto(review.get());
            LOGGER.info("END Find Random Page with Replacements: {}", response);
            return ResponseEntity
                .ok()
                .header(TOTAL_PAGES_HEADER, String.valueOf(Objects.requireNonNullElse(review.get().getNumPending(), 0)))
                .body(response);
        } else {
            LOGGER.info("END Find Random Page with Replacements: No Review");
            return ResponseEntity.noContent().build();
        }
    }
}