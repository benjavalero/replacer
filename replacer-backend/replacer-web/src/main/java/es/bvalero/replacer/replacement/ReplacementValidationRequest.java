package es.bvalero.replacer.replacement;

import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springdoc.api.annotations.ParameterObject;

@ParameterObject
@Data
class ReplacementValidationRequest {

    @Parameter(description = "Replacement to validate", required = true, example = "aún")
    @NotBlank
    String replacement;

    @Parameter(description = "If the custom replacement is case-sensitive", required = true, example = "false")
    boolean cs;
}
