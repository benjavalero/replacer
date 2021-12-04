package es.bvalero.replacer.page.index;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import java.time.LocalDate;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Sub-domain object representing a replacement (to be) indexed in the database */
@Value
@Builder
class IndexableReplacement {

    @Nullable
    Long id; // Nullable when still to be created in database

    @NonNull
    IndexablePageId indexablePageId;

    @NonNull
    String type;

    @NonNull
    String subtype;

    @With
    @NonNull
    Integer position;

    @With
    @NonNull
    String context;

    @With
    @NonNull
    LocalDate lastUpdate;

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

    boolean isOlderThan(IndexableReplacement replacement) {
        return isOlderThan(replacement.getLastUpdate());
    }

    boolean isOlderThan(LocalDate lastUpdate) {
        return this.lastUpdate.isBefore(lastUpdate);
    }

    static IndexableReplacement ofDummy(IndexablePage indexablePage) {
        Objects.requireNonNull(indexablePage.getLastUpdate());
        return IndexableReplacement
            .builder()
            .indexablePageId(indexablePage.getId())
            .type("")
            .subtype("")
            .position(0)
            .context("")
            .lastUpdate(indexablePage.getLastUpdate())
            .reviewer(REVIEWER_SYSTEM)
            .build();
    }

    boolean isDummy() {
        return (
            StringUtils.isEmpty(this.type) &&
            StringUtils.isEmpty(this.subtype) &&
            this.position == 0 &&
            StringUtils.isEmpty(this.context) &&
            REVIEWER_SYSTEM.equals(this.reviewer)
        );
    }
}
