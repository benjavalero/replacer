package es.bvalero.replacer.replacement.count.cache;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.repository.ResultCount;
import java.util.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;

@Value(staticConstructor = "of")
class KindCount {

    // Store internally the type counts in a map
    @Getter(AccessLevel.NONE)
    Map<ReplacementKind, TypeCount> typeCounts;

    static KindCount fromModel(Collection<ResultCount<ReplacementType>> counts) {
        final Map<ReplacementKind, TypeCount> typeCounts = new EnumMap<>(ReplacementKind.class);
        for (ResultCount<ReplacementType> count : counts) {
            ReplacementKind kind = count.getKey().getKind();
            TypeCount typeCount = typeCounts.computeIfAbsent(kind, k -> TypeCount.of());
            typeCount.add(count.getKey().getSubtype(), count.getCount());
        }
        return KindCount.of(typeCounts);
    }

    Collection<ResultCount<ReplacementType>> toModel() {
        final List<ResultCount<ReplacementType>> counts = new ArrayList<>();
        for (Map.Entry<ReplacementKind, TypeCount> entry : typeCounts.entrySet()) {
            for (Map.Entry<String, Integer> subEntry : entry.getValue().getSubtypeCounts().entrySet()) {
                ReplacementType type = ReplacementType.of(entry.getKey(), subEntry.getKey());
                counts.add(ResultCount.of(type, subEntry.getValue()));
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
    boolean contains(ReplacementKind kind) {
        return typeCounts.containsKey(kind);
    }

    @TestOnly
    TypeCount get(ReplacementKind kind) {
        return typeCounts.get(kind);
    }

    void removeTypeCount(ReplacementKind kind, String subtype) {
        if (this.typeCounts.containsKey(kind)) {
            TypeCount typeCount = this.typeCounts.get(kind);
            typeCount.remove(subtype);

            // Empty parent if children are empty
            if (typeCount.isEmpty()) {
                this.typeCounts.remove(kind);
            }
        }
    }

    void decrementSubtypeCount(ReplacementKind kind, String subtype) {
        if (this.typeCounts.containsKey(kind)) {
            // Empty parent if children are empty
            TypeCount typeCount = this.typeCounts.get(kind);
            if (!typeCount.decrementSubtypeCount(subtype) && typeCount.isEmpty()) {
                this.typeCounts.remove(kind);
            }
        }
    }
}
