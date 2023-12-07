package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springdoc.api.annotations.ParameterObject;

@ParameterObject
@Data
class ReplacementTypeFindRequest {

    @Parameter(description = "Text to replace", required = true, example = "aún")
    @NotBlank
    String replacement;

    @Parameter(description = "If the replacement is case-sensitive", required = true, example = "false")
    boolean cs;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
