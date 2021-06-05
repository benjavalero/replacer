package es.bvalero.replacer.common;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class UserParameters {

    @ApiParam(value = "Language", required = true)
    private WikipediaLanguage lang;

    @ApiParam(value = "Wikipedia user name", required = true, example = "Benjavalero")
    private String user;
}
