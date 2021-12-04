package es.bvalero.replacer.common;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.annotations.ApiParam;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserParameters {

    // TODO: Obsolete

    @ApiParam(value = "Language", required = true)
    @NotNull
    private WikipediaLanguage lang;

    @ApiParam(value = "Wikipedia user name", required = true, example = "Benjavalero")
    @NotNull
    private String user;
}
