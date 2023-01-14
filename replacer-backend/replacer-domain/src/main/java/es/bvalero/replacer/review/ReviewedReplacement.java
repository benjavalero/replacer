package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.replacement.IndexedCustomReplacement;
import es.bvalero.replacer.replacement.IndexedReplacement;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value
@Builder
public class ReviewedReplacement {

    @NonNull
    PageKey pageKey;

    @NonNull
    ReplacementType type;

    @Nullable
    Boolean cs; // Only for custom replacements

    int start;

    @NonNull
    String reviewer;

    boolean fixed;

    IndexedPage toPage() {
        return IndexedPage
            .builder()
            .pageKey(pageKey)
            .title("") // It will be set in a next indexation
            .lastUpdate(LocalDate.now())
            .build();
    }

    IndexedReplacement toReplacement() {
        return IndexedReplacement
            .builder()
            .type(type)
            .start(start)
            .context("") // It is not important when saving a review as we only want to update the reviewer
            .reviewer(reviewer)
            .pageKey(pageKey)
            .build();
    }

    IndexedCustomReplacement toCustomReplacement() {
        return IndexedCustomReplacement
            .builder()
            .replacement(type.getSubtype())
            .caseSensitive(Boolean.TRUE.equals(this.cs))
            .start(start)
            .reviewer(reviewer)
            .pageKey(pageKey)
            .build();
    }
}
