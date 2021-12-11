package es.bvalero.replacer.replacement.count;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

@Schema(description = "Replacement kind along with the related types and the page counts")
@Value(staticConstructor = "of")
class TypeCount implements Comparable<TypeCount> {

    // We use short aliases for the JSON properties in order to try to reduce the size of the response

    @Schema(description = "Replacement kind", required = true, example = "Ortografía")
    @JsonProperty("t")
    @NonNull
    String type;

    // Store internally the subtype counts in a sorted map
    @Getter(AccessLevel.NONE)
    Map<String, SubtypeCount> subtypeCounts = new TreeMap<>();

    @Schema(description = "List of page counts by type", required = true)
    @JsonProperty("l")
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

    @Override
    public int compareTo(TypeCount typeCount) {
        return this.type.compareTo(typeCount.type);
    }
}
