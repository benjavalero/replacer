package es.bvalero.replacer.review.dto;

import es.bvalero.replacer.common.dto.AccessTokenDto;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema
@Data
@NoArgsConstructor
public class SaveReviewRequest {

    @Schema(description = "Page reviewed", required = true)
    @Valid
    @NotNull
    private ReviewPageDto page;

    @Schema(description = "Review options", required = true)
    @Valid
    @NotNull
    private ReviewOptionsDto options;

    @Schema(description = "Mark as reviewed all page replacements despite the type in the options", required = true)
    private boolean reviewAllTypes;

    @Schema(description = "Access token authenticating the user to save the page", required = true)
    @ToString.Exclude
    @Valid
    @NotNull
    private AccessTokenDto accessToken;
}
