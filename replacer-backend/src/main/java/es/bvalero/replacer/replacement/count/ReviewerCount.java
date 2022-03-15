package es.bvalero.replacer.replacement.count;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema
@Value(staticConstructor = "of")
class ReviewerCount {

    @Schema(description = "Name of the user in Wikipedia", required = true, example = "Benjavalero")
    @NonNull
    String reviewer;

    @Schema(required = true, example = "1")
    int count;
}
