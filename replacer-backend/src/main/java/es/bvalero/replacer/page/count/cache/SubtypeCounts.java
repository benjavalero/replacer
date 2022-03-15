package es.bvalero.replacer.page.count.cache;

import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;

/** Class to store and access efficiently the page counts by subtype */
@Value(staticConstructor = "of")
class SubtypeCounts {

    // Store internally the subtype counts in a map for faster access
    Map<String, Integer> counts = new HashMap<>();

    void add(String subtype, int count) {
        this.counts.put(subtype, count);
    }

    void remove(String subtype) {
        this.counts.remove(subtype);
    }

    boolean isEmpty() {
        return this.counts.isEmpty();
    }

    @TestOnly
    int size() {
        return this.counts.size();
    }

    @TestOnly
    int get(String subtype) {
        return this.counts.getOrDefault(subtype, 0);
    }

    // Return false if the decrement produces an empty subtype
    boolean decrementSubtypeCount(String subtype) {
        int newCount = this.counts.getOrDefault(subtype, 0) - 1;
        if (newCount > 0) {
            // Update the subtype with the new count
            this.counts.put(subtype, newCount);
            return true;
        } else {
            // Remove the subtype count as in method "removeCachedReplacementCount"
            this.counts.remove(subtype);
            return false;
        }
    }
}
