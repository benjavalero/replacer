package es.bvalero.replacer.page;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import lombok.Value;

@Schema(description = "Count of pages to review grouped by kind")
@Value(staticConstructor = "of")
public class KindCount implements Comparable<KindCount> {

    // We use short aliases for the JSON properties in order to try to reduce the size of the response

    @Schema(type = "integer", description = "Replacement kind code", requiredMode = REQUIRED, example = "2")
    @JsonProperty("k")
    byte kind;

    // No need to store the list sorted
    @Schema(description = "Count of pages to review grouped by subtype for a given kind", requiredMode = REQUIRED)
    @JsonProperty("l")
    Collection<SubtypeCount> subtypeCounts;

    @Override
    public int compareTo(KindCount kindCount) {
        // For the moment we keep sorting by the kind name
        return ReplacementKind.valueOf(this.kind).name().compareTo(ReplacementKind.valueOf(kindCount.getKind()).name());
    }

    @Override
    public String toString() {
        return ReplacerUtils.toJson("k", this.kind, "l", this.subtypeCounts.size());
    }
}
