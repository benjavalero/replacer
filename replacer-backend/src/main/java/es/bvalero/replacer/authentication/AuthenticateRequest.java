package es.bvalero.replacer.authentication;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Request token and verifier to complete OAuth authentication")
@Data
@NoArgsConstructor
class AuthenticateRequest {

    @ApiParam(value = "Language of the Wikipedia in use", allowableValues = "es, gl", required = true, example = "es")
    @NotNull
    private WikipediaLanguage lang;

    @ApiModelProperty(required = true, example = "4360336e38cd5dc8e3f90ad797154275")
    @NotBlank
    private String token;

    @ApiModelProperty(required = true, example = "533f4ef5d94d89649626123db3dbec35")
    @NotBlank
    private String tokenSecret;

    @ApiModelProperty(required = true, example = "04fd9900f948d22e65c5618b2093cfc5")
    @NotBlank
    private String oauthVerifier;
}
