package es.bvalero.replacer.page.review;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Page and replacements to review")
@Value(staticConstructor = "of")
class PageReviewResponse {

    @Schema(required = true)
    @NonNull
    ReviewPage page;

    @Schema(description = "Collection of replacements to review", required = true)
    @NonNull
    Collection<ReviewReplacement> replacements;

    @Schema(required = true)
    @NonNull
    PageReviewSearch search;

    @Schema(description = "Number of pending pages to review of the given type", required = true, example = "1704147")
    @NonNull
    Long numPending;
}
