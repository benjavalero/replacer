package es.bvalero.replacer.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import es.bvalero.replacer.common.domain.ReplacementType;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Schema(description = "Reviewed replacement", name = "ReviewedReplacement")
@Data
@NoArgsConstructor
class ReviewedReplacementDto {

    @Schema(type = "integer", description = "Replacement kind", requiredMode = REQUIRED)
    byte kind;

    @Schema(description = "Replacement subtype", requiredMode = REQUIRED)
    @Size(max = ReplacementType.MAX_SUBTYPE_LENGTH)
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
