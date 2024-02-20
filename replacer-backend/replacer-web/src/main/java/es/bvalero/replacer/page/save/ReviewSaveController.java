package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.resolver.AuthenticatedUser;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.WikipediaException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
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
class ReviewSaveController {

    // Dependency injection
    private final ApplyCosmeticsService applyCosmeticsService;
    private final ReviewSaveService reviewSaveService;

    ReviewSaveController(ApplyCosmeticsService applyCosmeticsService, ReviewSaveService reviewSaveService) {
        this.applyCosmeticsService = applyCosmeticsService;
        this.reviewSaveService = reviewSaveService;
    }

    @Operation(summary = "Save a review: update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    ResponseEntity<Void> saveReview(
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
            reviewSaveService.markAsReviewed(reviewedReplacements, null);
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
            WikipediaPageSaveCommand pageSave = WikipediaPageSaveCommand
                .builder()
                .pageKey(pageKey)
                .sectionId(reviewedPage.getSectionId())
                .content(textToSave)
                .editSummary(summary)
                .queryTimestamp(queryTimestamp)
                .build();

            WikipediaPageSaveResult pageSaveResult = reviewSaveService.saveReviewContent(pageSave, user);
            reviewSaveService.markAsReviewed(reviewedReplacements, pageSaveResult);
        }

        return ResponseEntity.noContent().build();
    }
}
