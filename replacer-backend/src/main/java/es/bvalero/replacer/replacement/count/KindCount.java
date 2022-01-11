package es.bvalero.replacer.replacement.count;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Replacement kind along with the related types and the page counts")
@Value(staticConstructor = "of")
class KindCount implements Comparable<KindCount> {

    // We use short aliases for the JSON properties in order to try to reduce the size of the response

    @Schema(description = "Replacement kind", required = true, example = "Ortografía")
    @JsonProperty("t")
    @NonNull
    String kind;

    // No need to store the list sorted
    @Schema(description = "List of page counts by type", required = true)
    @JsonProperty("l")
    List<SubtypeCount> subtypeCounts = new ArrayList<>();

    void add(SubtypeCount subtypeCount) {
        this.subtypeCounts.add(subtypeCount);
    }

    @Override
    public int compareTo(KindCount kindCount) {
        return this.kind.compareTo(kindCount.kind);
    }
}
