package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.page.PageKey;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * An indexed replacement can be persisted and retrieved from persistence,
 * and compared against replacements from other sources like the ones found in a page.
 */
@Value
@Builder
public class IndexedReplacement {

    public static final String REVIEWER_SYSTEM = "system";

    @Nullable
    Integer id;

    @NonNull
    ReplacementType type;

    int start;

    @NonNull
    String context;

    @Nullable
    String reviewer;

    // Many-to-one relationship
    @NonNull
    PageKey pageKey;
}
