package es.bvalero.replacer.page;

import es.bvalero.replacer.replacement.IndexedReplacement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * An indexed page can be persisted and retrieved from persistence,
 * and compared against pages from other sources like Wikipedia or a dump.
 */
@Value
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class IndexedPage {

    // Key
    @NonNull
    @EqualsAndHashCode.Include
    PageKey pageKey;

    @NonNull
    String title;

    @NonNull
    LocalDate lastUpdate;

    // One-to-many relationship
    @NonNull
    @Builder.Default
    Collection<IndexedReplacement> replacements = new ArrayList<>();

    // Helper method to add replacements after creating the object while extracting the results from database
    void addReplacement(IndexedReplacement replacement) {
        this.replacements.add(replacement);
    }
}
