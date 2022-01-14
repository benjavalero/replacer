package es.bvalero.replacer.page.review;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Replacement to review")
@Value(staticConstructor = "of")
class ReviewReplacement {

    @Schema(description = "Position of the replacement in the content", required = true, example = "1776")
    int start;

    @Schema(description = "Text of the replacement", example = "aún", required = true)
    @NonNull
    String text;

    @Schema(description = "Collection of suggestions to fix the replacement", required = true)
    @NonNull
    Collection<ReviewSuggestion> suggestions;
}
