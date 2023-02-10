package es.bvalero.replacer.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import es.bvalero.replacer.user.AccessTokenDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Schema
@Data
class SaveReviewRequest {

    @Schema(description = "Page reviewed", requiredMode = REQUIRED)
    @Valid
    @NotNull
    private ReviewPage page;

    @Schema(description = "Reviewed replacements", requiredMode = REQUIRED)
    @Valid
    @NotNull
    @NotEmpty
    private Collection<ReviewedReplacementDto> reviewedReplacements;

    @Schema(description = "Access token authenticating the user to save the page", requiredMode = REQUIRED)
    @ToString.Exclude
    @Valid
    @NotNull
    private AccessTokenDto accessToken;
}
