package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Domain object representing a replacement (to be) indexed in the database */
@Value
@Builder
public class IndexableReplacement {

    private static final String REVIEWER_SYSTEM = "system";

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

    /* Named parameters to make easier the JDBC queries */

    WikipediaLanguage getLang() {
        return this.indexablePageId.getLang();
    }

    Integer getPageId() {
        return this.indexablePageId.getPageId();
    }

    /* Other helper getters */

    public boolean isToBeReviewed() {
        return this.reviewer == null;
    }

    public boolean isSystemReviewed() {
        return REVIEWER_SYSTEM.equals(this.reviewer);
    }

    @TestOnly
    public IndexableReplacement setSystemReviewed() {
        return withReviewer(REVIEWER_SYSTEM);
    }

    public boolean isOlderThan(IndexableReplacement replacement) {
        return isOlderThan(replacement.getLastUpdate());
    }

    public boolean isOlderThan(LocalDate lastUpdate) {
        return this.lastUpdate.isBefore(lastUpdate);
    }

    public static IndexableReplacement ofDummy(IndexablePage indexablePage) {
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

    public boolean isDummy() {
        return (
            StringUtils.isEmpty(this.type) &&
            StringUtils.isEmpty(this.subtype) &&
            this.position == 0 &&
            StringUtils.isEmpty(this.context) &&
            REVIEWER_SYSTEM.equals(this.reviewer)
        );
    }
}
