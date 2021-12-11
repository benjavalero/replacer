package es.bvalero.replacer.page.review;

import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;

@ParameterObject
@Data
@NoArgsConstructor
class ReplacementValidationRequest {

    @Parameter(description = "Replacement to validate", required = true, example = "aún")
    @Size(max = 100)
    @NotBlank
    String replacement;

    @Parameter(description = "If the custom replacement is case-sensitive", required = true, example = "false")
    boolean cs;
}
