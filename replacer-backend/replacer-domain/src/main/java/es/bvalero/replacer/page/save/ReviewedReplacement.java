package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.finder.CustomType;
import es.bvalero.replacer.finder.ReplacementType;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.page.IndexedCustomReplacement;
import es.bvalero.replacer.page.IndexedReplacement;
import es.bvalero.replacer.page.IndexedReplacementStatus;
import es.bvalero.replacer.page.ReviewType;
import es.bvalero.replacer.wikipedia.WikipediaPageSaveResult;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value
@Builder
class ReviewedReplacement {

    @NonNull
    PageKey pageKey;

    @NonNull
    ReplacementType type;

    // It is used to identify the replacement along with the type and page
    int start;

    @NonNull
    String reviewer;

    // True if fixed. False if reviewed with no changes.
    // It is used to know which replacement types to include in the edit summary.
    @With(onMethod_ = @TestOnly)
    boolean fixed;

    IndexedReplacement toIndexedReplacement(@Nullable WikipediaPageSaveResult saveResult) {
        assert type instanceof StandardType;
        return saveResult == null ? toNotModifiedReplacement() : toModifiedReplacement(saveResult);
    }

    private IndexedReplacement toModifiedReplacement(WikipediaPageSaveResult saveResult) {
        return IndexedReplacement.builder()
            .type((StandardType) type)
            .start(start)
            .context("") // It is not important when saving a review as we only want to update the reviewer
            .reviewer(reviewer)
            .reviewType(ReviewType.MODIFIED)
            .reviewTimestamp(saveResult.getNewTimestamp().toLocalDateTime())
            .oldRevId(saveResult.getOldRevisionId())
            .newRevId(saveResult.getNewRevisionId())
            .status(IndexedReplacementStatus.REVIEWED)
            .pageKey(pageKey)
            .build();
    }

    private IndexedReplacement toNotModifiedReplacement() {
        return IndexedReplacement.builder()
            .type((StandardType) type)
            .start(start)
            .context("") // It is not important when saving a review as we only want to update the reviewer
            .reviewer(reviewer)
            .reviewType(ReviewType.NOT_MODIFIED)
            .reviewTimestamp(LocalDateTime.now(ZoneId.systemDefault()))
            .status(IndexedReplacementStatus.REVIEWED)
            .pageKey(pageKey)
            .build();
    }

    IndexedCustomReplacement toCustomReplacement(@Nullable WikipediaPageSaveResult saveResult) {
        assert type instanceof CustomType;
        return saveResult == null ? toNotModifiedCustomReplacement() : toModifiedCustomReplacement(saveResult);
    }

    IndexedCustomReplacement toModifiedCustomReplacement(WikipediaPageSaveResult saveResult) {
        return IndexedCustomReplacement.builder()
            .type((CustomType) type)
            .start(start)
            .reviewer(reviewer)
            .reviewType(ReviewType.MODIFIED)
            .reviewTimestamp(saveResult.getNewTimestamp().toLocalDateTime())
            .oldRevId(saveResult.getOldRevisionId())
            .newRevId(saveResult.getNewRevisionId())
            .pageKey(pageKey)
            .build();
    }

    IndexedCustomReplacement toNotModifiedCustomReplacement() {
        return IndexedCustomReplacement.builder()
            .type((CustomType) type)
            .start(start)
            .reviewer(reviewer)
            .reviewType(ReviewType.NOT_MODIFIED)
            .reviewTimestamp(LocalDateTime.now(ZoneId.systemDefault()))
            .pageKey(pageKey)
            .build();
    }
}
