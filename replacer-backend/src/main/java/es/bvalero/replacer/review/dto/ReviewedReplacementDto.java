package es.bvalero.replacer.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Schema(description = "Reviewed replacement")
@Data
@NoArgsConstructor
public class ReviewedReplacementDto {

    @Schema(description = "Replacement kind", required = true)
    byte kind;

    @Schema(description = "Replacement subtype", required = true)
    @NotNull
    String subtype;

    @Schema(description = "If the replacement is case-sensitive. Only for custom replacements.")
    @Nullable
    Boolean cs;

    @Schema(description = "Replacement start position", required = true)
    int start;

    @Schema(description = "True if fixed. False if reviewed with no changes.", required = true)
    boolean fixed;
}
