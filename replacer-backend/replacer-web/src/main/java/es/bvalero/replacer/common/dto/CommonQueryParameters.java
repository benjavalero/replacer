package es.bvalero.replacer.common.dto;

import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;

/**
 * DTO containing the query parameters common to all REST calls.
 * All fields are mandatory. Frontend will send them with an interceptor.
 * All fields are also hidden, in order not to appear in the generated OpenAPI specification and implementation.
 */
@ParameterObject
@Data
@NoArgsConstructor
public class CommonQueryParameters {

    @Parameter(description = "Language of the Wikipedia in use", required = true, example = "es")
    @NotNull
    private Language lang;

    @Parameter(description = "Name of the user in Wikipedia", required = true, example = "Benjavalero")
    @Size(max = 100)
    @NotNull
    private String user;
}
