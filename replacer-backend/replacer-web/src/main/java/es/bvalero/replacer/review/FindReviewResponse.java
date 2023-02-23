package es.bvalero.replacer.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(description = "Page and replacements to review")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value(staticConstructor = "of")
class FindReviewResponse {

    @Schema(requiredMode = REQUIRED)
    @NonNull
    ReviewPage page;

    @Schema(description = "Collection of replacements to review", requiredMode = REQUIRED)
    @NonNull
    Collection<ReviewReplacement> replacements;

    @Schema(description = "Number of pending pages to review of the given type", example = "1704147")
    @Nullable
    Integer numPending;
}
