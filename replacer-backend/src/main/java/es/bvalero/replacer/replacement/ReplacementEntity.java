package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.Nullable;

/**
 * A replacement in the database related to a page.
 */
@Data
@NoArgsConstructor // Needed by ModelMapper
@AllArgsConstructor
public class ReplacementEntity implements Serializable {

    public static final String TYPE_CUSTOM = "Personalizado";
    public static final String REVIEWER_SYSTEM = "system";

    @Nullable
    private Long id; // Nullable when still to be created in database

    private int pageId;
    private String lang;
    private String type;
    private String subtype;
    private int position;
    private String context;
    private LocalDate lastUpdate;

    @Nullable
    private String reviewer;

    private String title;

    // Temporary field not in database to know the status of the replacement while indexing
    // Values: C - Create, U - Update, D - Delete, K - Keep
    @Nullable
    @With
    private transient String cudAction;

    @TestOnly
    public ReplacementEntity(int pageId, String type, String subtype, int position) {
        this.pageId = pageId;
        this.type = type;
        this.subtype = subtype;
        this.position = position;
        this.lastUpdate = LocalDate.now();
    }

    @TestOnly
    ReplacementEntity(int pageId, String type, String subtype, int position, String reviewer) {
        this.pageId = pageId;
        this.type = type;
        this.subtype = subtype;
        this.position = position;
        this.lastUpdate = LocalDate.now();
        this.reviewer = reviewer;
    }

    boolean isToBeReviewed() {
        return this.reviewer == null;
    }

    boolean isOlderThan(LocalDate date) {
        return this.lastUpdate.isBefore(date);
    }

    boolean isSystemReviewed() {
        return REVIEWER_SYSTEM.equals(this.reviewer);
    }

    void setToCreate() {
        this.cudAction = "C";
    }

    public boolean isToCreate() {
        return "C".equals(this.cudAction);
    }

    void setToDelete() {
        this.cudAction = "D";
    }

    @TestOnly
    ReplacementEntity withToDelete() {
        return this.withCudAction("D");
    }

    public boolean isToDelete() {
        return "D".equals(this.cudAction);
    }

    void setToUpdateDate() {
        this.cudAction = "UD";
    }

    public boolean isToUpdateDate() {
        return "UD".equals(this.cudAction);
    }

    void setToUpdateContext() {
        this.cudAction = "UC";
    }

    public boolean isToUpdateContext() {
        return "UC".equals(this.cudAction);
    }

    boolean isToUpdate() {
        return this.cudAction != null && this.cudAction.contains("U");
    }

    void setToKeep() {
        this.cudAction = "K";
    }

    static ReplacementEntity createDummy(int pageId, WikipediaLanguage lang, LocalDate lastUpdate) {
        return new ReplacementEntity(
            null,
            pageId,
            lang.getCode(),
            "",
            "",
            0,
            null,
            lastUpdate,
            REVIEWER_SYSTEM,
            null,
            "C"
        );
    }

    boolean isDummy() {
        return (
            StringUtils.isEmpty(this.type) &&
            StringUtils.isEmpty(this.subtype) &&
            this.position == 0 &&
            REVIEWER_SYSTEM.equals(this.reviewer)
        );
    }

    public static ReplacementEntity createCustomSystemReviewed(int pageId, WikipediaLanguage lang, String subtype) {
        return createCustomReviewed(pageId, lang, subtype, REVIEWER_SYSTEM);
    }

    public static ReplacementEntity createCustomReviewed(
        int pageId,
        WikipediaLanguage lang,
        String subtype,
        String reviewer
    ) {
        return new ReplacementEntity(
            null,
            pageId,
            lang.getCode(),
            TYPE_CUSTOM,
            subtype,
            0,
            null,
            LocalDate.now(),
            reviewer,
            null,
            "C"
        );
    }
}
