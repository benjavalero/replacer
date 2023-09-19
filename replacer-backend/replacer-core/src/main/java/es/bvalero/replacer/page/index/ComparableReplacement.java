package es.bvalero.replacer.page.index;

import static es.bvalero.replacer.finder.Replacement.CONTEXT_THRESHOLD;
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
        return (getType().equals(that.type) && (hasSameStart(that) || hasSameContext(that)));
    }

    private boolean hasSameStart(ComparableReplacement that) {
        // 0 is the default start for legacy replacements
        if (this.start != 0 || that.start != 0) {
            return this.start == that.start;
        } else {
            return false;
        }
    }

    // We accept two contexts as the same if they are equal and close enough from each other
    private boolean hasSameContext(ComparableReplacement that) {
        // An empty string is the default context for legacy or migrated replacements
        if (StringUtils.isNotBlank(this.context) || StringUtils.isNotBlank(that.context)) {
            return Objects.equals(this.context, that.getContext()) && this.distance(that) <= 2 * CONTEXT_THRESHOLD;
        } else {
            return false;
        }
    }

    private int distance(ComparableReplacement that) {
        // We don't know which one is at left or at right
        return Math.min(Math.abs(this.start - that.getEnd()), Math.abs(that.start - this.getEnd()));
    }

    private int getEnd() {
        // This is a maximum approximation as the replacement context could be truncated by the page content
        return this.start + this.context.length() - 2 * CONTEXT_THRESHOLD;
    }
}
