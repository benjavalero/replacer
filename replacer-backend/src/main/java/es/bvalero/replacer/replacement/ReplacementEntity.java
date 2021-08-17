package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.WikipediaLanguage;
import java.time.LocalDate;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
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

    public boolean isToBeReviewed() {
        return this.reviewer == null;
    }

    public boolean isOlderThan(LocalDate date) {
        return this.lastUpdate.isBefore(date);
    }

    @TestOnly
    public ReplacementEntity setSystemReviewed() {
        return withReviewer(REVIEWER_SYSTEM);
    }

    public boolean isSystemReviewed() {
        return REVIEWER_SYSTEM.equals(this.reviewer);
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

    public ReplacementEntity updateLastUpdate(LocalDate lastUpdate) {
        return withLastUpdate(lastUpdate).withCudAction("UD");
    }

    public boolean isToUpdateDate() {
        return "UD".equals(this.cudAction);
    }

    public ReplacementEntity updateContext(String context) {
        return withContext(context).withCudAction("UC");
    }

    public boolean isToUpdateContext() {
        return "UC".equals(this.cudAction);
    }

    public ReplacementEntity updatePosition(int position) {
        // Action for position, context and title is the same
        return withPosition(position).withCudAction("UC");
    }

    public ReplacementEntity updateTitle(String title) {
        // Action for position, context and title is the same
        return withTitle(title).withCudAction("UC");
    }

    public boolean isToUpdate() {
        return this.cudAction != null && this.cudAction.startsWith("U");
    }

    public ReplacementEntity setToKeep() {
        return withCudAction("K");
    }

    public static ReplacementEntity ofDummy(int pageId, WikipediaLanguage lang, LocalDate lastUpdate) {
        return ReplacementEntity
            .builder()
            .lang(lang.getCode())
            .pageId(pageId)
            .type("")
            .subtype("")
            .position(0)
            .lastUpdate(lastUpdate)
            .reviewer(REVIEWER_SYSTEM)
            .build()
            .setToCreate();
    }

    public boolean isDummy() {
        return (
            StringUtils.isEmpty(this.type) &&
            StringUtils.isEmpty(this.subtype) &&
            this.position == 0 &&
            REVIEWER_SYSTEM.equals(this.reviewer)
        );
    }
}
