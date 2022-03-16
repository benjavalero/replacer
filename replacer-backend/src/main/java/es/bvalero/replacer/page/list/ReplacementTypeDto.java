package es.bvalero.replacer.page.list;

import es.bvalero.replacer.common.domain.ReplacementType;
import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;

@ParameterObject
@Data
@NoArgsConstructor
class ReplacementTypeDto {

    @Parameter(description = "Replacement kind code", required = true, example = "2")
    private byte kind;

    @Parameter(description = "Replacement subtype", required = true, example = "aún")
    @NotBlank
    private String subtype;

    ReplacementType toDomain() {
        return ReplacementType.of(this.getKind(), this.getSubtype());
    }
}
