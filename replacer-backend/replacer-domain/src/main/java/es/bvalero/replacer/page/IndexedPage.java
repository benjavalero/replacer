package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.PageKey;
import java.time.LocalDate;
import java.util.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;
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

    // Indexation status
    @Builder.Default
    IndexedPageStatus status = IndexedPageStatus.UNDEFINED;

    // One-to-many relationship
    @NonNull
    @Builder.Default
    Collection<IndexedReplacement> replacements = new ArrayList<>();

    // One-to-many relationship
    // We will not retrieve these replacements from the database,
    // but we will use this collection to add them.
    @NonNull
    @Builder.Default
    Collection<IndexedCustomReplacement> customReplacements = new ArrayList<>();

    // Helper method to add replacements after creating the object while extracting the results from database
    public void addReplacement(IndexedReplacement replacement) {
        this.replacements.add(replacement);
    }

    public boolean isPageToSave() {
        return (
            this.getStatus() != IndexedPageStatus.INDEXED ||
            this.getReplacements().stream().anyMatch(r -> r.getStatus() != IndexedReplacementStatus.INDEXED)
        );
    }

    // For the sake of the tests, we will perform a deep comparison.
    @TestOnly
    public static boolean compare(IndexedPage expected, IndexedPage actual) {
        if (
            !expected.equals(actual) ||
            !Objects.equals(expected.getTitle(), actual.getTitle()) ||
            !Objects.equals(expected.getLastUpdate(), actual.getLastUpdate()) ||
            !Objects.equals(expected.getStatus(), actual.getStatus()) ||
            expected.getReplacements().size() != actual.getReplacements().size()
        ) {
            return false;
        }

        List<IndexedReplacement> expectedList = expected
            .getReplacements()
            .stream()
            .sorted(Comparator.comparingInt(IndexedReplacement::getStart))
            .toList();
        List<IndexedReplacement> actualList = actual
            .getReplacements()
            .stream()
            .sorted(Comparator.comparingInt(IndexedReplacement::getStart))
            .toList();
        for (int i = 0; i < expectedList.size(); i++) {
            if (!Objects.equals(expectedList.get(i), actualList.get(i))) {
                return false;
            }
        }
        return true;
    }
}
