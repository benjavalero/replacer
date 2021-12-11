package es.bvalero.replacer.common.dto;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;

/** DTO containing the query parameters common to all REST calls even if they are not used */
@ParameterObject
@Data
@NoArgsConstructor
public class CommonQueryParameters {

    @Parameter(description = "Language of the Wikipedia in use", required = true, example = "es")
    @NotNull
    private WikipediaLanguage lang;

    @Parameter(description = "Name of the user in Wikipedia", required = true, example = "Benjavalero")
    @Size(max = 100)
    @NotBlank
    private String user;
}
