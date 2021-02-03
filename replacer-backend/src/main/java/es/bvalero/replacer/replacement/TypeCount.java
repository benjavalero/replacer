package es.bvalero.replacer.replacement;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
class TypeCount implements Comparable<TypeCount> {

    @JsonProperty("t")
    String type;

    @Getter(AccessLevel.NONE)
    Map<String, SubtypeCount> subtypeCounts = new TreeMap<>();

    @JsonProperty("l")
    List<SubtypeCount> getSubtypeCounts() {
        return new ArrayList<>(this.subtypeCounts.values());
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

    Optional<SubtypeCount> get(String subtype) {
        return Optional.ofNullable(subtypeCounts.get(subtype));
    }

    @Override
    public int compareTo(TypeCount typeCount) {
        return this.type.compareTo(typeCount.type);
    }
}
