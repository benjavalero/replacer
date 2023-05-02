package es.bvalero.replacer.common.dto;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.UserId;
import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springdoc.api.annotations.ParameterObject;

/**
 * DTO containing the query parameters common to all REST calls.
 * All fields are mandatory. Frontend will send them with an interceptor.
 */
@ParameterObject
@Data
public class CommonQueryParameters {

    @Parameter(description = "Name of the user in Wikipedia", required = true, example = "Benjavalero")
    @NotNull
    private String user;

    /* Helper method to get the parameters as a domain user ID */
    public UserId getUserId(String lang) {
        return UserId.of(WikipediaLanguage.valueOfCode(lang), user);
    }
}
