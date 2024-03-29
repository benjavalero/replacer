package es.bvalero.replacer.replacement.count;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema(description = "Count of reviewed/unreviewed replacements")
@Value(staticConstructor = "of")
class ReplacementCount {

    @Schema(requiredMode = REQUIRED, example = "1")
    int count;

    @Override
    public String toString() {
        return Integer.toString(this.count);
    }
}
