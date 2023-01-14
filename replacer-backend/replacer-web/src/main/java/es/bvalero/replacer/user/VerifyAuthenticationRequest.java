package es.bvalero.replacer.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request token and OAuth verifier to complete the authorization process")
@Data
@NoArgsConstructor
class VerifyAuthenticationRequest {

    @Schema
    @Valid
    @NotNull
    private RequestTokenDto requestToken;

    @Schema(requiredMode = REQUIRED, example = "04fd9900f948d22e65c5618b2093cfc5")
    @NotBlank
    private String oauthVerifier;
}
