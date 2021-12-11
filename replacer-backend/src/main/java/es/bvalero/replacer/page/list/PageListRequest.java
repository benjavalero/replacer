package es.bvalero.replacer.page.list;

import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;

@ParameterObject
@Data
@NoArgsConstructor
class PageListRequest {

    @Parameter(description = "Replacement kind", example = "Ortografía")
    @NotBlank
    private String type;

    @Parameter(description = "Replacement subtype", example = "aún")
    @NotBlank
    private String subtype;
}
