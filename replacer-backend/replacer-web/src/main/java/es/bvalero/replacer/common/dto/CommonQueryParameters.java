package es.bvalero.replacer.common.dto;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springdoc.api.annotations.ParameterObject;

/**
 * DTO containing the query parameters common to all REST calls.
 * All fields are mandatory. Frontend will send them with an interceptor.
 */
@ParameterObject
@Data
public class CommonQueryParameters {

    @Parameter(description = "Language of the Wikipedia in use", required = true, example = "es")
    @NotNull
    private String lang;

    @Parameter(description = "Name of the user in Wikipedia", required = true, example = "Benjavalero")
    @Size(max = 40)
    @NotNull
    private String user;

    /* Helper method to get the language as a domain object */
    public WikipediaLanguage getWikipediaLanguage() {
        return WikipediaLanguage.valueOfCode(lang);
    }
}
