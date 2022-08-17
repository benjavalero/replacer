package es.bvalero.replacer.review.dto;

import es.bvalero.replacer.common.dto.AccessTokenDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
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

    @Schema(description = "Reviewed replacements", required = true)
    @Valid
    @NotNull
    @NotEmpty
    private Collection<ReviewedReplacementDto> reviewedReplacements;

    @Schema(description = "Access token authenticating the user to save the page", required = true)
    @ToString.Exclude
    @Valid
    @NotNull
    private AccessTokenDto accessToken;
}
