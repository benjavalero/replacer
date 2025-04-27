package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.resolver.AuthenticatedUser;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.WikipediaException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Page")
@Slf4j
@PrimaryAdapter
@RestController
@RequestMapping("api/page")
class PageSaveController {

    // Dependency injection
    private final PageSaveApi pageSaveApi;

    PageSaveController(PageSaveApi pageSaveApi) {
        this.pageSaveApi = pageSaveApi;
    }

    @Operation(summary = "Save a review: update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    ResponseEntity<Void> saveReview(
        @Parameter(description = "Page ID", example = "1") @PathVariable("id") int pageId,
        @AuthenticatedUser User user,
        @Valid @RequestBody ReviewedPageDto reviewedPageDto
    ) throws WikipediaException {
        LOGGER.info("POST Save Reviewed Page: {} - {}", pageId, reviewedPageDto);

        ReviewedPage reviewedPage = mapReviewedPage(pageId, reviewedPageDto, user);

        pageSaveApi.save(reviewedPage, user);

        return ResponseEntity.noContent().build();
    }

    private ReviewedPage mapReviewedPage(int pageId, ReviewedPageDto dto, User user) {
        PageKey pageKey = PageKey.of(user.getId().getLang(), pageId);
        return ReviewedPage.builder()
            .pageKey(pageKey)
            .title(dto.getTitle())
            .content(dto.getContent())
            .sectionId(dto.getSectionId())
            .queryTimestamp(dto.getQueryTimestamp() == null ? null : WikipediaTimestamp.of(dto.getQueryTimestamp()))
            .reviewedReplacements(
                mapReviewedReplacements(pageKey, dto.getReviewedReplacements(), dto.getSectionOffset(), user)
            )
            .build();
    }

    private Collection<ReviewedReplacement> mapReviewedReplacements(
        PageKey pageKey,
        Collection<ReviewedReplacementDto> reviewed,
        @Nullable Integer sectionOffset,
        User user
    ) {
        return reviewed.stream().map(r -> mapReviewedReplacement(pageKey, r, sectionOffset, user)).toList();
    }

    private ReviewedReplacement mapReviewedReplacement(
        PageKey pageKey,
        ReviewedReplacementDto reviewed,
        @Nullable Integer sectionOffset,
        User user
    ) {
        ReplacementKind replacementKind = ReplacementKind.valueOf(reviewed.getKind());
        ReplacementType replacementType = replacementKind == ReplacementKind.CUSTOM
            ? CustomType.of(reviewed.getSubtype(), Objects.requireNonNull(reviewed.getCs()))
            : StandardType.of(replacementKind, reviewed.getSubtype());
        int offset = Objects.requireNonNullElse(sectionOffset, 0);
        return ReviewedReplacement.builder()
            .pageKey(pageKey)
            .type(replacementType)
            .start(offset + reviewed.getStart())
            .reviewer(user.getId().getUsername())
            .fixed(reviewed.isFixed())
            .build();
    }
}
