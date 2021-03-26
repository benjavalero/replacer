package es.bvalero.replacer.page;

import es.bvalero.replacer.common.WikipediaLanguage;
import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
class UserParameters {

    @ApiParam(value = "Language", required = true)
    private WikipediaLanguage lang;

    @ApiParam(value = "Wikipedia user name", required = true, example = "Benjavalero")
    private String user;
}
