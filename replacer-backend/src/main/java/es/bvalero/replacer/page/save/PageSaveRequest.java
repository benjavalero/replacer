package es.bvalero.replacer.page.save;

import es.bvalero.replacer.page.review.PageReviewSearch;
import es.bvalero.replacer.page.review.ReviewPage;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema(description = "Page to update and mark as reviewed. Empty content is equivalent to review with no changes.")
@Data
@NoArgsConstructor
class PageSaveRequest {

    @Schema(description = "Page to review", required = true)
    @Valid
    @NotNull
    private ReviewPage page;

    @Schema(description = "Search options of the replacements to review", required = true)
    @Valid
    @NotNull
    private PageReviewSearch search;

    @Schema(required = true, example = "f8e520e8669a2d65e094d649a96427ff")
    @ToString.Exclude
    @NotNull
    private String token;

    @Schema(required = true, example = "36dd90e87c59acc138ee0c38487e975af6da141e")
    @ToString.Exclude
    @NotNull
    private String tokenSecret;
}
