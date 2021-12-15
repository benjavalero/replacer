package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.TestOnly;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class LanguageCount {

    // Store internally the type counts in a sorted map
    private final Map<String, TypeCount> typeCounts;

    static LanguageCount build(Collection<ResultCount<ReplacementType>> counts) {
        final Map<String, TypeCount> typeCounts = new TreeMap<>();
        for (ResultCount<ReplacementType> count : counts) {
            String type = count.getKey().getKind().getLabel();
            TypeCount typeCount = typeCounts.computeIfAbsent(type, TypeCount::of);
            typeCount.add(SubtypeCount.of(count.getKey().getSubtype(), count.getCount()));
        }
        return new LanguageCount(typeCounts);
    }

    Collection<TypeCount> getTypeCounts() {
        return this.typeCounts.values();
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
    boolean contains(String type) {
        return typeCounts.containsKey(type);
    }

    @TestOnly
    TypeCount get(String type) {
        return typeCounts.get(type);
    }

    void removeTypeCount(String type, String subtype) {
        if (this.typeCounts.containsKey(type)) {
            TypeCount typeCount = this.typeCounts.get(type);
            typeCount.remove(subtype);

            // Empty parent if children are empty
            if (typeCount.isEmpty()) {
                this.typeCounts.remove(type);
            }
        }
    }

    void decrementSubtypeCount(String type, String subtype) {
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
