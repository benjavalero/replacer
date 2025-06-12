package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import es.bvalero.replacer.replacement.IndexedCustomReplacement;
import es.bvalero.replacer.replacement.IndexedReplacement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** A reviewed page. The page fields are only mandatory when saving the page with changes. */
@Value
@Builder
public class ReviewedPage {

    @NonNull
    PageKey pageKey;

    @Nullable
    String title;

    @Nullable
    String content;

    @Nullable
    Integer sectionId;

    @Nullable
    WikipediaTimestamp queryTimestamp;

    @NonNull
    Collection<ReviewedReplacement> reviewedReplacements;

    private ReviewedPage(
        PageKey pageKey,
        @Nullable String title,
        @Nullable String content,
        @Nullable Integer sectionId,
        @Nullable WikipediaTimestamp queryTimestamp,
        Collection<ReviewedReplacement> reviewedReplacements
    ) {
        // Implement the private constructor to perform validations when building by Lombok
        this.pageKey = pageKey;
        this.title = title;
        this.content = content;
        this.sectionId = sectionId;
        this.queryTimestamp = queryTimestamp;
        this.reviewedReplacements = reviewedReplacements;

        if (isReviewedWithoutChanges()) {
            if (
                content != null ||
                title != null ||
                sectionId != null ||
                queryTimestamp != null ||
                reviewedReplacements.stream().anyMatch(ReviewedReplacement::isFixed)
            ) {
                throw new IllegalArgumentException("Unnecessary fields to save a reviewed page without changes");
            }
        } else {
            if (
                content == null ||
                title == null ||
                queryTimestamp == null ||
                reviewedReplacements.stream().noneMatch(ReviewedReplacement::isFixed)
            ) {
                throw new IllegalArgumentException("Missing mandatory fields to save a reviewed page with changes");
            }
        }
    }

    boolean isReviewedWithoutChanges() {
        return Objects.isNull(this.content);
    }

    FinderPage toFinderPage() {
        return FinderPage.of(pageKey, Objects.requireNonNull(title), Objects.requireNonNull(content));
    }

    IndexedPage toIndexedPage(@Nullable WikipediaPageSaveResult saveResult) {
        return saveResult == null ? toNotModifiedPage() : toModifiedPage(saveResult);
    }

    private IndexedPage toModifiedPage(WikipediaPageSaveResult saveResult) {
        return IndexedPage.builder()
            .pageKey(pageKey)
            .title(title)
            .lastUpdate(saveResult.getNewTimestamp().toLocalDate())
            .replacements(toIndexedReplacements(saveResult))
            .customReplacements(toIndexedCustomReplacements(saveResult))
            .status(IndexedPageStatus.UPDATE)
            .build();
    }

    private IndexedPage toNotModifiedPage() {
        return IndexedPage.builder()
            .pageKey(pageKey)
            .title("") // Not important, it will not be updated
            .lastUpdate(LocalDate.now()) // Not important, it will not be updated.
            .replacements(toIndexedReplacements(null))
            .customReplacements(toIndexedCustomReplacements(null))
            .status(IndexedPageStatus.INDEXED)
            .build();
    }

    private Collection<IndexedReplacement> toIndexedReplacements(@Nullable WikipediaPageSaveResult saveResult) {
        return reviewedReplacements
            .stream()
            .filter(r -> r.getType() instanceof StandardType)
            .map(r -> r.toIndexedReplacement(saveResult))
            .toList();
    }

    private Collection<IndexedCustomReplacement> toIndexedCustomReplacements(
        @Nullable WikipediaPageSaveResult saveResult
    ) {
        return reviewedReplacements
            .stream()
            .filter(r -> r.getType() instanceof CustomType)
            .map(r -> r.toCustomReplacement(saveResult))
            .toList();
    }
}
