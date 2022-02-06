package es.bvalero.replacer.replacement.count;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.bvalero.replacer.common.domain.ReplacementKind;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;

@Schema(description = "Replacement kind along with the related types and the page counts")
@Value(staticConstructor = "of")
class KindCount implements Comparable<KindCount> {

    // We use short aliases for the JSON properties in order to try to reduce the size of the response

    @Schema(description = "Replacement kind code", required = true, example = "2")
    @JsonProperty("k")
    byte kind;

    // No need to store the list sorted
    @Schema(description = "List of page counts by type", required = true)
    @JsonProperty("l")
    List<TypeCount> typeCounts = new ArrayList<>();

    void add(TypeCount typeCount) {
        this.typeCounts.add(typeCount);
    }

    @Override
    public int compareTo(KindCount kindCount) {
        // For the moment we keep sorting by the kind name
        return ReplacementKind.valueOf(this.kind).name().compareTo(ReplacementKind.valueOf(kindCount.getKind()).name());
    }
}
