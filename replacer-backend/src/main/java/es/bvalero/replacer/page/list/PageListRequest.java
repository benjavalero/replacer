package es.bvalero.replacer.page.list;

import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;

@ParameterObject
@Data
@NoArgsConstructor
class PageListRequest {

    @Parameter(description = "Replacement kind code", example = "2")
    @NotNull
    private Byte type;

    @Parameter(description = "Replacement subtype", example = "aún")
    @NotBlank
    private String subtype;
}
