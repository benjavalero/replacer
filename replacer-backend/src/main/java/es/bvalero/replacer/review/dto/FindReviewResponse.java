package es.bvalero.replacer.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(description = "Page and replacements to review")
@Value(staticConstructor = "of")
public class FindReviewResponse {

    @Schema(required = true)
    @NonNull
    ReviewPageDto page;

    @Schema(description = "Collection of replacements to review", required = true)
    @NonNull
    Collection<ReviewReplacementDto> replacements;

    @Schema(required = true)
    @NonNull
    ReviewOptionsDto options;

    @Schema(description = "Number of pending pages to review of the given type", required = true, example = "1704147")
    @Nullable
    Integer numPending;
}
