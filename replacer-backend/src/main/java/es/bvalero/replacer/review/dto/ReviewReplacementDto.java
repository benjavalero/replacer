package es.bvalero.replacer.review.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(description = "Replacement to review")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value(staticConstructor = "of")
class ReviewReplacementDto {

    @Schema(description = "Position of the replacement in the content", required = true, example = "1776")
    int start;

    @Schema(description = "Text of the replacement", example = "aún", required = true)
    @NonNull
    String text;

    @Schema(description = "Kind of the replacement", example = "2", required = true)
    byte kind;

    @Schema(description = "Subtype of the replacement", example = "habia", required = true)
    @NonNull
    String subtype;

    @Schema(description = "If the replacement is case-sensitive. Only for custom replacements.")
    @Nullable
    Boolean cs;

    @Schema(description = "Collection of suggestions to fix the replacement", required = true)
    @NonNull
    Collection<ReviewSuggestionDto> suggestions;
}
