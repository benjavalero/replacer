package es.bvalero.replacer.replacement.count.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;

@Value(staticConstructor = "of")
class TypeCount {

    // Store internally the subtype counts in a map
    Map<String, Long> subtypeCounts = new HashMap<>();

    void add(String subtype, Long count) {
        this.subtypeCounts.put(subtype, count);
    }

    void remove(String subtype) {
        this.subtypeCounts.remove(subtype);
    }

    boolean isEmpty() {
        return this.subtypeCounts.isEmpty();
    }

    @TestOnly
    int size() {
        return this.subtypeCounts.size();
    }

    Optional<Long> get(String subtype) {
        return Optional.ofNullable(subtypeCounts.get(subtype));
    }

    // Return false if the decrement produces an empty subtype
    boolean decrementSubtypeCount(String subtype) {
        long newCount = this.subtypeCounts.getOrDefault(subtype, 0L) - 1;
        if (newCount > 0) {
            // Update the subtype with the new count
            this.subtypeCounts.put(subtype, newCount);
            return true;
        } else {
            // Remove the subtype count as in method "removeCachedReplacementCount"
            this.subtypeCounts.remove(subtype);
            return false;
        }
    }
}
