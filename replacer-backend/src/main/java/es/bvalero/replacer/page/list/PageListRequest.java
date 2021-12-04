package es.bvalero.replacer.page.list;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.annotations.ApiParam;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class PageListRequest {

    @ApiParam(value = "Language", required = true)
    @NotNull
    private WikipediaLanguage lang;

    @ApiParam(value = "Wikipedia user name", required = true, example = "Benjavalero")
    @NotNull
    private String user;

    @ApiParam(value = "Replacement type", example = "Ortografía")
    @NotNull
    private String type;

    @ApiParam(value = "Replacement subtype", example = "aún")
    @NotNull
    private String subtype;
}
