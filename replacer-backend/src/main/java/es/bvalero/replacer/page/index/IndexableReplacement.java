package es.bvalero.replacer.page.index;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.ReplacementType;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
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

    boolean isSystemReviewed() {
        return REVIEWER_SYSTEM.equals(this.reviewer);
    }

    @TestOnly
    IndexableReplacement setSystemReviewed() {
        return withReviewer(REVIEWER_SYSTEM);
    }

    /* If two replacements have the same position or context they will be considered equivalent but NOT EQUAL */
    boolean isSame(IndexableReplacement that) {
        return (
            indexablePageId.equals(that.indexablePageId) &&
            type.equals(that.type) &&
            (position == that.position || isSameContext(context, that.context))
        );
    }

    private boolean isSameContext(String context1, String context2) {
        if (StringUtils.isNotBlank(context1) || StringUtils.isNotBlank(context2)) {
            return Objects.equals(context1, context2);
        } else {
            return false;
        }
    }
}
