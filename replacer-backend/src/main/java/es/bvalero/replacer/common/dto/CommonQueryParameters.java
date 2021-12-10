package es.bvalero.replacer.common.dto;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.annotations.ApiParam;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO containing the query parameters common to all REST calls even if they are not used */
@Data
@NoArgsConstructor
public class CommonQueryParameters {

    @ApiParam(value = "Language of the Wikipedia in use", allowableValues = "es, gl", required = true, example = "es")
    @NotNull
    private WikipediaLanguage lang;

    @ApiParam(value = "Name of the user in Wikipedia", required = true, example = "Benjavalero")
    @Size(max = 100)
    @NotNull
    private String user;
}
