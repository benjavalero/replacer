package es.bvalero.replacer.page.review;

import io.swagger.annotations.ApiParam;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class ReplacementValidationRequest {

    @ApiParam(value = "Replacement to validate", required = true, example = "aún")
    @Size(max = 100)
    @NotNull
    String replacement;

    @ApiParam(value = "If the custom replacement is case-sensitive")
    boolean cs;
}
