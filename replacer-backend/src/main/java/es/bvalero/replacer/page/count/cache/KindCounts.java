package es.bvalero.replacer.page.count.cache;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.repository.ResultCount;
import java.util.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;

/** Class to store and access efficiently the page counts by kind */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class KindCounts {

    // Store internally the kind counts in a map for faster access
    @Getter(AccessLevel.NONE)
    Map<ReplacementKind, SubtypeCounts> counts;

    static KindCounts fromModel(Collection<ResultCount<ReplacementType>> resultCounts) {
        final Map<ReplacementKind, SubtypeCounts> kindCounts = new EnumMap<>(ReplacementKind.class);
        for (ResultCount<ReplacementType> count : resultCounts) {
            ReplacementKind kind = count.getKey().getKind();
            SubtypeCounts subtypeCounts = kindCounts.computeIfAbsent(kind, k -> SubtypeCounts.of());
            subtypeCounts.add(count.getKey().getSubtype(), count.getCount());
        }
        return new KindCounts(kindCounts);
    }

    Collection<ResultCount<ReplacementType>> toModel() {
        final List<ResultCount<ReplacementType>> resultCounts = new ArrayList<>();
        for (Map.Entry<ReplacementKind, SubtypeCounts> entry : this.counts.entrySet()) {
            for (Map.Entry<String, Integer> subEntry : entry.getValue().getCounts().entrySet()) {
                ReplacementType type = ReplacementType.of(entry.getKey(), subEntry.getKey());
                resultCounts.add(ResultCount.of(type, subEntry.getValue()));
            }
        }
        return resultCounts;
    }

    @TestOnly
    boolean isEmpty() {
        return this.counts.isEmpty();
    }

    @TestOnly
    int size() {
        return this.counts.size();
    }

    @TestOnly
    boolean contains(ReplacementKind kind) {
        return this.counts.containsKey(kind);
    }

    @TestOnly
    SubtypeCounts get(ReplacementKind kind) {
        return this.counts.getOrDefault(kind, SubtypeCounts.of());
    }

    void removeTypeCount(ReplacementKind kind, String subtype) {
        if (this.counts.containsKey(kind)) {
            SubtypeCounts subtypeCounts = this.counts.get(kind);
            subtypeCounts.remove(subtype);

            // Empty parent if children are empty
            if (subtypeCounts.isEmpty()) {
                this.counts.remove(kind);
            }
        }
    }

    void decrementSubtypeCount(ReplacementKind kind, String subtype) {
        if (this.counts.containsKey(kind)) {
            // Empty parent if children are empty
            SubtypeCounts subtypeCounts = this.counts.get(kind);
            if (!subtypeCounts.decrementSubtypeCount(subtype) && subtypeCounts.isEmpty()) {
                this.counts.remove(kind);
            }
        }
    }
}
