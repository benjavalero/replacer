package es.bvalero.replacer.replacement;

import es.bvalero.replacer.page.PageKey;
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
    String replacement;

    boolean caseSensitive;

    int start;

    @Nullable
    String reviewer;

    // Many-to-one relationship
    @NonNull
    PageKey pageKey;

    // To store in the JDBC repository
    @SuppressWarnings("unused")
    byte getCs() {
        return getCs(caseSensitive);
    }

    static byte getCs(boolean caseSensitive) {
        return (byte) (caseSensitive ? 1 : 0);
    }
}
