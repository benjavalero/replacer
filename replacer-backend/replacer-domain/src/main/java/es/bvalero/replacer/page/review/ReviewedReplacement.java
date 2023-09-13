package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.replacement.IndexedCustomReplacement;
import es.bvalero.replacer.replacement.IndexedReplacement;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value
@Builder
public class ReviewedReplacement {

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
    boolean fixed;

    IndexedReplacement toReplacement() {
        assert type instanceof StandardType;
        return IndexedReplacement
            .builder()
            .type((StandardType) type)
            .start(start)
            .context("") // It is not important when saving a review as we only want to update the reviewer
            .reviewer(reviewer)
            .pageKey(pageKey)
            .build();
    }

    IndexedCustomReplacement toCustomReplacement() {
        assert type instanceof CustomType;
        return IndexedCustomReplacement
            .builder()
            .type((CustomType) type)
            .start(start)
            .reviewer(reviewer)
            .pageKey(pageKey)
            .build();
    }
}
