package es.bvalero.replacer.replacement.count;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Count the pages to review grouped by subtype")
@Value(staticConstructor = "of")
class SubtypeCount {

    // We use short aliases for the JSON properties in order to try to reduce the size of the response

    @Schema(description = "Replacement subtype", required = true, example = "habia")
    @JsonProperty("s")
    @NonNull
    String subtype;

    @Schema(description = "Count the pages to review containing this subtype", required = true, example = "1")
    @JsonProperty("c")
    int count;
}
