package es.bvalero.replacer.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

@Schema(description = "Reviewed replacement", name = "ReviewedReplacement")
@Data
class ReviewedReplacementDto {

    @Schema(type = "integer", description = "Replacement kind", requiredMode = REQUIRED)
    byte kind;

    @Schema(description = "Replacement subtype", requiredMode = REQUIRED)
    @NotNull
    String subtype;

    @Schema(description = "If the replacement is case-sensitive. Only for custom replacements.")
    @Nullable
    Boolean cs;

    @Schema(description = "Replacement start position", requiredMode = REQUIRED)
    int start;

    @Schema(description = "True if fixed. False if reviewed with no changes.", requiredMode = REQUIRED)
    boolean fixed;
}
