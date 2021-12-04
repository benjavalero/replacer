package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.annotations.ApiParam;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class ReplacementValidationRequest {

    @ApiParam(value = "Language", required = true)
    @NotNull
    private WikipediaLanguage lang;

    @ApiParam(value = "Wikipedia user name", required = true, example = "Benjavalero")
    @NotNull
    private String user;

    @ApiParam(value = "Replacement to validate", required = true, example = "aún")
    @Size(max = 100)
    @NotNull
    String replacement;

    @ApiParam(value = "If the custom replacement is case-sensitive")
    boolean cs;
}
