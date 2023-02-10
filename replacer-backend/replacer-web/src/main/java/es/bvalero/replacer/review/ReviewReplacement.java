package es.bvalero.replacer.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Replacement to review")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value(staticConstructor = "of")
class ReviewReplacement {

    @Schema(description = "Position of the replacement in the content", requiredMode = REQUIRED, example = "1776")
    int start;

    @Schema(description = "Text of the replacement", example = "aún", requiredMode = REQUIRED)
    @NonNull
    String text;

    @Schema(type = "integer", description = "Kind of the replacement", example = "2", requiredMode = REQUIRED)
    byte kind;

    @Schema(description = "Subtype of the replacement", example = "habia", requiredMode = REQUIRED)
    @NonNull
    String subtype;

    @Schema(description = "Collection of suggestions to fix the replacement", requiredMode = REQUIRED)
    @NonNull
    Collection<ReviewSuggestion> suggestions;
}
