package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AuthenticatedUser;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageSave;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Page")
@Slf4j
@RestController
@RequestMapping("api/page")
public class ReviewSaveController {

    // Dependency injection
    private final ApplyCosmeticsService applyCosmeticsService;
    private final ReviewSaveService reviewSaveService;

    public ReviewSaveController(ApplyCosmeticsService applyCosmeticsService, ReviewSaveService reviewSaveService) {
        this.applyCosmeticsService = applyCosmeticsService;
        this.reviewSaveService = reviewSaveService;
    }

    @Operation(summary = "Save a review: update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    public ResponseEntity<Void> saveReview(
        @Parameter(description = "Page ID", example = "1") @PathVariable("id") int pageId,
        @AuthenticatedUser User user,
        @Valid @RequestBody ReviewedPage reviewedPage
    ) throws WikipediaException {
        LOGGER.info("POST Save Reviewed Page: {}", reviewedPage);

        // Validate the request body
        reviewedPage.validate();

        Collection<ReviewedReplacement> reviewedReplacements = ReviewedMapper.fromDto(
            pageId,
            reviewedPage.getReviewedReplacements(),
            reviewedPage.getSectionOffset(),
            user
        );
        if (reviewedPage.isReviewedWithoutChanges()) {
            reviewSaveService.markAsReviewed(reviewedReplacements, false);
        } else {
            PageKey pageKey = PageKey.of(user.getId().getLang(), pageId);
            FinderPage page = reviewedPage.toFinderPage(pageKey);

            // Apply cosmetic changes
            String textToSave = applyCosmeticsService.applyCosmeticChanges(page);
            boolean applyCosmetics = !textToSave.equals(reviewedPage.getContent());

            // Edit summary
            Collection<ReplacementType> fixedReplacementTypes = reviewedReplacements
                .stream()
                .filter(ReviewedReplacement::isFixed)
                .map(ReviewedReplacement::getType)
                .collect(Collectors.toUnmodifiableSet());
            String summary = reviewSaveService.buildEditSummary(fixedReplacementTypes, applyCosmetics);

            // Upload new content to Wikipedia
            WikipediaTimestamp queryTimestamp = WikipediaTimestamp.of(
                Objects.requireNonNull(reviewedPage.getQueryTimestamp())
            );
            WikipediaPageSave pageSave = WikipediaPageSave
                .builder()
                .pageKey(pageKey)
                .sectionId(reviewedPage.getSectionId())
                .content(textToSave)
                .editSummary(summary)
                .queryTimestamp(queryTimestamp)
                .build();

            reviewSaveService.saveReviewContent(pageSave, user);
            reviewSaveService.markAsReviewed(reviewedReplacements, true);
        }

        return ResponseEntity.noContent().build();
    }
}
