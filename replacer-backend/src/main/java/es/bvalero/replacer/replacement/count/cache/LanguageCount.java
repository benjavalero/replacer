package es.bvalero.replacer.replacement.count.cache;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.repository.ResultCount;
import java.util.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.TestOnly;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class LanguageCount {

    // Store internally the type counts in a sorted map
    private final Map<ReplacementKind, TypeCount> typeCounts;

    static LanguageCount fromModel(Collection<ResultCount<ReplacementType>> counts) {
        final Map<ReplacementKind, TypeCount> typeCounts = new TreeMap<>();
        for (ResultCount<ReplacementType> count : counts) {
            ReplacementKind type = count.getKey().getKind();
            TypeCount typeCount = typeCounts.computeIfAbsent(type, TypeCount::of);
            typeCount.add(SubtypeCount.of(count.getKey().getSubtype(), count.getCount()));
        }
        return new LanguageCount(typeCounts);
    }

    Collection<ResultCount<ReplacementType>> toModel() {
        List<ResultCount<ReplacementType>> counts = new ArrayList<>();
        for (TypeCount typeCount : typeCounts.values()) {
            for (SubtypeCount subtypeCount : typeCount.getSubtypeCounts()) {
                ReplacementType type = ReplacementType.of(typeCount.getType(), subtypeCount.getSubtype());
                counts.add(ResultCount.of(type, subtypeCount.getCount()));
            }
        }
        return counts;
    }

    @TestOnly
    boolean isEmpty() {
        return typeCounts.isEmpty();
    }

    @TestOnly
    int size() {
        return typeCounts.size();
    }

    @TestOnly
    boolean contains(ReplacementKind type) {
        return typeCounts.containsKey(type);
    }

    @TestOnly
    TypeCount get(ReplacementKind type) {
        return typeCounts.get(type);
    }

    void removeTypeCount(ReplacementKind type, String subtype) {
        if (this.typeCounts.containsKey(type)) {
            TypeCount typeCount = this.typeCounts.get(type);
            typeCount.remove(subtype);

            // Empty parent if children are empty
            if (typeCount.isEmpty()) {
                this.typeCounts.remove(type);
            }
        }
    }

    void decrementSubtypeCount(ReplacementKind type, String subtype) {
        if (this.typeCounts.containsKey(type)) {
            TypeCount typeCount = this.typeCounts.get(type);
            typeCount
                .get(subtype)
                .ifPresent(subtypeCount -> {
                    long newCount = subtypeCount.getCount() - 1;
                    if (newCount > 0) {
                        // Update the subtype with the new count
                        typeCount.add(subtypeCount.withCount(newCount));
                    } else {
                        // Remove the subtype count as in method "removeCachedReplacementCount"
                        typeCount.remove(subtype);

                        // Empty parent if children are empty
                        if (typeCount.isEmpty()) {
                            this.typeCounts.remove(type);
                        }
                    }
                });
        }
    }
}
