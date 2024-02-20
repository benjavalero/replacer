package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.page.PageKey;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * An indexed custom replacement can be persisted and retrieved from persistence,
 * and compared against replacements from other sources like the ones found in a page.
 */
@Value
@Builder
public class IndexedCustomReplacement {

    @Nullable
    Integer id;

    @NonNull
    CustomType type;

    int start;

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

    // To store in the JDBC repository
    public byte getCs() {
        return getCs(this.type.isCaseSensitive());
    }

    static byte getCs(boolean caseSensitive) {
        return (byte) (caseSensitive ? 1 : 0);
    }
}
