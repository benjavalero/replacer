package es.bvalero.replacer.replacement;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Count of reviewed replacements by reviewer")
@Value(staticConstructor = "of")
public class ReviewerCount {

    @Schema(requiredMode = REQUIRED, example = "Benjavalero")
    @NonNull
    String reviewer;

    @Schema(requiredMode = REQUIRED, example = "1")
    int count;
}
