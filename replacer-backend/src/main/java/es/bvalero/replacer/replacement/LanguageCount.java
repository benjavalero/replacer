package es.bvalero.replacer.replacement;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.TestOnly;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class LanguageCount {

    private final Map<String, TypeCount> typeCounts;

    static LanguageCount build(List<TypeSubtypeCount> counts) {
        final Map<String, TypeCount> typeCounts = new TreeMap<>();
        for (TypeSubtypeCount count : counts) {
            String type = count.getType();
            TypeCount typeCount = typeCounts.computeIfAbsent(type, TypeCount::of);
            typeCount.add(SubtypeCount.of(count.getSubtype(), count.getCount()));
        }
        return new LanguageCount(typeCounts);
    }

    List<TypeCount> getTypeCounts() {
        return List.copyOf(this.typeCounts.values());
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
                .ifPresent(
                    subtypeCount -> {
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
                    }
                );
        }
    }
}
