package es.bvalero.replacer.replacement;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.lang.Nullable;

/**
 * A replacement in the database related to a page.
 */
@Value
@Builder
public class ReplacementEntity {

    static final String REVIEWER_SYSTEM = "system";

    @Nullable
    Long id; // Nullable when still to be created in database

    String lang;
    int pageId;
    String type;
    String subtype;

    @With(AccessLevel.PRIVATE)
    int position;

    @With
    String context;

    @VisibleForTesting
    @With
    LocalDate lastUpdate;

    @Nullable
    @With
    String reviewer;

    @With
    String title;

    // Temporary field not in database to know the status of the replacement while indexing
    // Values: C - Create, U - Update, D - Delete, K - Keep
    @Nullable
    @With(AccessLevel.PRIVATE)
    String cudAction;

    @TestOnly
    public static ReplacementEntity of(int pageId, String type, String subtype, int position) {
        return ReplacementEntity
            .builder()
            .pageId(pageId)
            .type(type)
            .subtype(subtype)
            .position(position)
            .lastUpdate(LocalDate.now())
            .build();
    }

    public boolean isOlderThan(LocalDate date) {
        return this.lastUpdate.isBefore(date);
    }

    public ReplacementEntity setToCreate() {
        return withCudAction("C");
    }

    public boolean isToCreate() {
        return "C".equals(this.cudAction);
    }

    public ReplacementEntity setToDelete() {
        return withCudAction("D");
    }

    public boolean isToDelete() {
        return "D".equals(this.cudAction);
    }
}
