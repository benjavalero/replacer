package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.dto.AccessTokenDto;
import es.bvalero.replacer.common.dto.PageReviewOptionsDto;
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
    private PageReviewOptionsDto options;

    @Schema(description = "Access token authenticating the user to save the page", required = true)
    @ToString.Exclude
    @NotNull
    private AccessTokenDto accessToken;
}
