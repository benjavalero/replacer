package es.bvalero.replacer.page.index;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.ComparableReplacement;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Replacement (to be) indexed in the database */
@Value
@Builder
class IndexableReplacement implements ComparableReplacement {

    @With
    @Nullable
    Integer id; // Nullable when still to be created in database

    @NonNull
    WikipediaPageId pageId;

    @NonNull
    ReplacementType type;

    @With
    int start;

    @With
    @NonNull
    String context;

    @With
    @Nullable
    String reviewer;

    /* To mark the replacements already "touched" while comparing indexable pages */
    @EqualsAndHashCode.Exclude
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
}
