package es.bvalero.replacer.replacement.count;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.jspecify.annotations.NonNull;

@Schema(description = "Count of reviewed custom replacements by type")
@Value(staticConstructor = "of")
class CustomCount {

    @Schema(description = "Custom replacement text", requiredMode = REQUIRED, example = "habia")
    @NonNull
    String subtype;

    @Schema(description = "Whether the replacement is case-sensitive", requiredMode = REQUIRED, example = "false")
    boolean cs;

    @Schema(requiredMode = REQUIRED, example = "150")
    int count;
}
