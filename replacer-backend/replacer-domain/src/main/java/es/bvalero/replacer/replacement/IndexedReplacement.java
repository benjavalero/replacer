package es.bvalero.replacer.replacement;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.page.PageKey;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * An indexed replacement can be persisted and retrieved from persistence,
 * and compared against replacements from other sources like the ones found in a page.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
public class IndexedReplacement {

    public static final String REVIEWER_SYSTEM = "system";

    @Nullable
    Integer id;

    @NonNull
    StandardType type;

    int start;

    @NonNull
    String context;

    @Nullable
    String reviewer;

    @Builder.Default
    ReviewType reviewType = ReviewType.UNKNOWN;

    // This fields will be not-null for reviewed not-legacy replacements
    @Nullable
    LocalDateTime reviewTimestamp;

    @Nullable
    Integer oldRevId;

    @Nullable
    Integer newRevId;

    // Many-to-one relationship
    @NonNull
    PageKey pageKey;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
