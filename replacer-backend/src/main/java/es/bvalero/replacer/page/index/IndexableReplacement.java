package es.bvalero.replacer.page.index;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.ReplacementType;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Replacement (to be) indexed in the database */
@Value
@Builder
class IndexableReplacement {

    @With
    @Nullable
    Integer id; // Nullable when still to be created in database

    @NonNull
    IndexablePageId indexablePageId;

    @NonNull
    ReplacementType type;

    @With
    int position;

    @With
    @NonNull
    String context;

    @With
    @Nullable
    String reviewer;

    /* To mark the replacements already "touched" while comparing indexable pages */
    @With
    boolean touched;

    /* Other helper getters */

    boolean isToBeReviewed() {
        return this.reviewer == null;
    }

    boolean isReviewed() {
        return !isToBeReviewed();
    }

    boolean isSystemReviewed() {
        return REVIEWER_SYSTEM.equals(this.reviewer);
    }

    @TestOnly
    IndexableReplacement setSystemReviewed() {
        return withReviewer(REVIEWER_SYSTEM);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexablePageId, type);
    }

    /* If two replacements have the same position or context they will be considered equal */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexableReplacement that = (IndexableReplacement) o;
        return (
            indexablePageId.equals(that.indexablePageId) &&
            type.equals(that.type) &&
            (position == that.position || context.equals(that.context))
        );
    }
}
