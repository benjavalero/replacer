package es.bvalero.replacer.replacement;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
class TypeCount implements Comparable<TypeCount> {
    @JsonProperty("t")
    private final String type;

    @JsonProperty("l")
    private final List<SubtypeCount> subtypeCounts = new LinkedList<>();

    TypeCount(String type) {
        this.type = type;
    }

    void add(SubtypeCount subtypeCount) {
        this.subtypeCounts.add(subtypeCount);
    }

    void remove(String subtype) {
        this.subtypeCounts.removeIf(st -> st.getSubtype().equals(subtype));
    }

    boolean isEmpty() {
        return this.subtypeCounts.isEmpty();
    }

    Optional<SubtypeCount> get(String subtype) {
        return subtypeCounts.stream().filter(st -> st.getSubtype().equals(subtype)).findAny();
    }

    @Override
    public int compareTo(TypeCount typeCount) {
        return this.type.compareTo(typeCount.type);
    }
}
