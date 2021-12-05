package es.bvalero.replacer.replacement.count;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;

@Value(staticConstructor = "of")
class TypeCount implements Comparable<TypeCount> {

    @ApiModelProperty(value = "Replacement type", required = true, example = "Ortografía")
    @JsonProperty("t")
    String type;

    @Getter(AccessLevel.NONE)
    Map<String, SubtypeCount> subtypeCounts = new TreeMap<>();

    @ApiModelProperty(value = "List of subtype counts", required = true)
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

    @TestOnly
    int size() {
        return this.subtypeCounts.size();
    }

    Optional<SubtypeCount> get(String subtype) {
        return Optional.ofNullable(subtypeCounts.get(subtype));
    }

    @Override
    public int compareTo(TypeCount typeCount) {
        return this.type.compareTo(typeCount.type);
    }
}
