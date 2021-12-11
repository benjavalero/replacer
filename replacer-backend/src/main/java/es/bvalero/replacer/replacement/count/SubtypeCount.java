package es.bvalero.replacer.replacement.count;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import lombok.With;
import org.springframework.lang.NonNull;

@Schema(description = "Page counts by replacement subtype")
@Value(staticConstructor = "of")
class SubtypeCount {

    // We use short aliases for the JSON properties in order to try to reduce the size of the response

    @Schema(description = "Replacement subtype", required = true, example = "habia")
    @JsonProperty("s")
    @NonNull
    String subtype;

    @Schema(description = "Number of pages containing this subtype to review", required = true, example = "1")
    @JsonProperty("c")
    @With
    @NonNull
    Long count;
}
