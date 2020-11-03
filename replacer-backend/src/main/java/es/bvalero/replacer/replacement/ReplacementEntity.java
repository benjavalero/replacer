package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.TestOnly;

/**
 * A replacement in the database related to a page.
 */
@Data
@NoArgsConstructor // Needed by ModelMapper
@AllArgsConstructor
public class ReplacementEntity implements Serializable {
    public static final String TYPE_CUSTOM = "Personalizado";
    static final String TYPE_DELETE = "delete";
    public static final String REVIEWER_SYSTEM = "system";

    private Long id;
    private int pageId;
    private String lang;
    private String type;
    private String subtype;
    private int position;
    private String context;
    private LocalDate lastUpdate;
    private String reviewer;
    private String title;

    public ReplacementEntity(int pageId, WikipediaLanguage lang, String type, String subtype, int position) {
        this.pageId = pageId;
        this.lang = lang.getCode();
        this.type = type;
        this.subtype = subtype;
        this.position = position;
        this.lastUpdate = LocalDate.now();
        this.reviewer = null;
    }

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

    private boolean isSystemReviewed() {
        return REVIEWER_SYSTEM.equals(this.reviewer);
    }

    boolean isUserReviewed() {
        return !isToBeReviewed() && !isSystemReviewed();
    }

    boolean isCustom() {
        return TYPE_CUSTOM.equals(this.type);
    }

    public boolean isToInsert() {
        return this.id == null;
    }

    public boolean isToDelete() {
        return TYPE_DELETE.equals(this.type);
    }

    public boolean isToUpdate() {
        return !isToInsert() && !isToDelete();
    }
}
