package es.bvalero.replacer.replacement.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema
@Value(staticConstructor = "of")
class ReplacementCount {

    @Schema(required = true, example = "1")
    int count;
}
