package es.bvalero.replacer.page.save;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

@Schema(description = "Reviewed replacement", name = "ReviewedReplacement")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
class ReviewedReplacementDto {

    @Schema(type = "integer", description = "Replacement kind", requiredMode = REQUIRED)
    byte kind;

    @Schema(description = "Replacement subtype", requiredMode = REQUIRED)
    @NotNull
    String subtype;

    @Schema(
        description = "If the replacement is case-sensitive. Only for custom replacements.",
        requiredMode = NOT_REQUIRED
    )
    @Nullable
    Boolean cs;

    @Schema(description = "Replacement start position", requiredMode = REQUIRED)
    int start;

    @Schema(description = "True if fixed. False if reviewed with no changes.", requiredMode = REQUIRED)
    boolean fixed;
}
