package es.bvalero.replacer.replacement.count.cache;

import es.bvalero.replacer.common.domain.ReplacementKind;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class TypeCount {

    @NonNull
    ReplacementKind type;

    // Store internally the subtype counts in a sorted map
    @Getter(AccessLevel.NONE)
    Map<String, SubtypeCount> subtypeCounts = new TreeMap<>();

    Collection<SubtypeCount> getSubtypeCounts() {
        return this.subtypeCounts.values();
    }

    void add(SubtypeCount subtypeCount) {
        this.subtypeCounts.put(subtypeCount.getSubtype(), subtypeCount);
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

    Optional<SubtypeCount> get(String subtype) {
        return Optional.ofNullable(subtypeCounts.get(subtype));
    }
}
