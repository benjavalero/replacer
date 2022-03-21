package es.bvalero.replacer.review.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(description = "Suggestion for a replacement to review")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value(staticConstructor = "of")
class ReviewSuggestion {

    @Schema(description = "Fix proposed for a replacement", required = true, example = "aun")
    @NonNull
    String text;

    @Schema(description = "Description to explain the motivation of the fix", example = "incluso, aunque")
    @Nullable
    String comment;
}
