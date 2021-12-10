package es.bvalero.replacer.authentication;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.TestOnly;

@ApiModel(description = "Request DTO containing the request token and the verifier of OAuth authentication")
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @TestOnly
    static AuthenticateRequest of(WikipediaLanguage lang, RequestToken requestToken, String oAuthVerifier) {
        return new AuthenticateRequest(lang, requestToken.getToken(), requestToken.getTokenSecret(), oAuthVerifier);
    }

    RequestToken getRequestToken() {
        return RequestToken.of(token, tokenSecret);
    }
}
