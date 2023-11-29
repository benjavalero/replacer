package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.resolver.AuthenticatedUser;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Page")
@Slf4j
@PrimaryAdapter
@RestController
@RequestMapping("api/page")
class ReviewFindController {

    static final String TOTAL_PAGES_HEADER = "X-Pagination-Total-Pages";

    // Dependency injection
    private final ReviewNoTypeFinder reviewNoTypeFinder;
    private final ReviewTypeFinder reviewTypeFinder;
    private final ReviewCustomFinder reviewCustomFinder;

    ReviewFindController(
        ReviewNoTypeFinder reviewNoTypeFinder,
        ReviewTypeFinder reviewTypeFinder,
        ReviewCustomFinder reviewCustomFinder
    ) {
        this.reviewNoTypeFinder = reviewNoTypeFinder;
        this.reviewTypeFinder = reviewTypeFinder;
        this.reviewCustomFinder = reviewCustomFinder;
    }

    @Operation(summary = "Find a random page and the replacements to review")
    @GetMapping(value = "/random")
    ResponseEntity<Page> findRandomPageWithReplacements(
        @AuthenticatedUser User user,
        @Valid ReviewOptionsDto optionsDto
    ) {
        LOGGER.info("START Find Random Page with Replacements. Options: {}", optionsDto);
        ReviewOptions options = ReviewMapper.fromDto(optionsDto, user);
        Optional<Review> review =
            switch (options.getKind()) {
                case EMPTY -> reviewNoTypeFinder.findRandomPageReview(options);
                case CUSTOM -> reviewCustomFinder.findRandomPageReview(options);
                default -> reviewTypeFinder.findRandomPageReview(options);
            };
        return buildResponse(review);
    }

    @Operation(summary = "Find a page and the replacements to review")
    @GetMapping(value = "/{id}")
    ResponseEntity<Page> findPageReviewById(
        @Parameter(description = "Page ID", example = "6980716") @PathVariable("id") int pageId,
        @AuthenticatedUser User user,
        @Valid ReviewOptionsDto optionsDto
    ) {
        LOGGER.info("START Find Page Review by ID {}. Options: {}", pageId, optionsDto);
        ReviewOptions options = ReviewMapper.fromDto(optionsDto, user);
        PageKey pageKey = PageKey.of(user.getId().getLang(), pageId);
        Optional<Review> review =
            switch (options.getKind()) {
                case EMPTY -> reviewNoTypeFinder.findPageReview(pageKey, options);
                case CUSTOM -> reviewCustomFinder.findPageReview(pageKey, options);
                default -> reviewTypeFinder.findPageReview(pageKey, options);
            };
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
