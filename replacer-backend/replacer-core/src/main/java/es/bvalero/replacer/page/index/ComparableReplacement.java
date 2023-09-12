package es.bvalero.replacer.page.index;

import static es.bvalero.replacer.replacement.IndexedReplacement.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.replacement.IndexedReplacement;
import java.util.Objects;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Wrapper for a replacement indexed in the database during the comparison, as we need to keep track of the status. */
@Value
@Builder(access = AccessLevel.PRIVATE)
class ComparableReplacement {

    @EqualsAndHashCode.Exclude
    @Nullable
    Integer id;

    @NonNull
    StandardType type;

    @With
    int start;

    @With
    @NonNull
    String context;

    @Nullable
    String reviewer;

    // Many-to-one relationship
    @NonNull
    PageKey pageKey;

    /* To mark the replacements already "touched" while comparing indexable pages */
    @EqualsAndHashCode.Exclude
    @With
    boolean touched;

    static ComparableReplacement of(IndexedReplacement replacement) {
        return ComparableReplacement
            .builder()
            .id(replacement.getId())
            .pageKey(replacement.getPageKey())
            .type(replacement.getType())
            .start(replacement.getStart())
            .context(replacement.getContext())
            .reviewer(replacement.getReviewer())
            .touched(false)
            .build();
    }

    static ComparableReplacement of(Replacement replacement) {
        assert replacement.getType() instanceof StandardType;
        return of(
            IndexedReplacement
                .builder()
                .pageKey(replacement.getPage().getPageKey())
                .type((StandardType) replacement.getType())
                .start(replacement.getStart())
                .context(replacement.getContext())
                .build()
        );
    }

    IndexedReplacement toDomain() {
        return IndexedReplacement
            .builder()
            .id(id)
            .pageKey(pageKey)
            .type(type)
            .start(start)
            .context(context)
            .reviewer(reviewer)
            .build();
    }

    /* Other helper getters */

    boolean isToBeReviewed() {
        return this.reviewer == null;
    }

    boolean isObsolete() {
        return !touched && (isToBeReviewed() || isSystemReviewed());
    }

    private boolean isSystemReviewed() {
        return REVIEWER_SYSTEM.equals(this.reviewer);
    }

    /*
     * Comparison: If two replacements have the same position or context they will be considered equivalent but NOT EQUAL.
     */
    boolean isSame(ComparableReplacement that) {
        return (
            getType().equals(that.getType()) &&
            (isSameStart(getStart(), that.getStart()) || isSameContext(getContext(), that.getContext()))
        );
    }

    private boolean isSameStart(int start1, int start2) {
        // 0 is the default start for legacy replacements
        if (start1 != 0 || start2 != 0) {
            return start1 == start2;
        } else {
            return false;
        }
    }

    private boolean isSameContext(String context1, String context2) {
        // An empty string is the default context for legacy or migrated replacements
        if (StringUtils.isNotBlank(context1) || StringUtils.isNotBlank(context2)) {
            return Objects.equals(context1, context2);
        } else {
            return false;
        }
    }
}
