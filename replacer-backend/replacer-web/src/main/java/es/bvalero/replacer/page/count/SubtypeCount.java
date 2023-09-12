package es.bvalero.replacer.page.count;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Count of pages to review grouped by subtype")
@Value(staticConstructor = "of")
class SubtypeCount {

    // We use short aliases for the JSON properties in order to try to reduce the size of the response

    @Schema(description = "Replacement subtype", requiredMode = REQUIRED, example = "habia")
    @JsonProperty("s")
    @NonNull
    String subtype;

    @Schema(description = "Count of pages to review containing this subtype", requiredMode = REQUIRED, example = "1")
    @JsonProperty("c")
    int count;
}
