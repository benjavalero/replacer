package es.bvalero.replacer.replacement.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema
@Value(staticConstructor = "of")
class ReplacementCount {

    @Schema(required = true, example = "1")
    int count;
}
