package es.bvalero.replacer.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Request token and OAuth verifier to complete the authorization process")
@Data
class VerifyAuthorizationRequest {

    @Schema
    @Valid
    @NotNull
    private RequestTokenDto requestToken;

    @Schema(requiredMode = REQUIRED, example = "04fd9900f948d22e65c5618b2093cfc5")
    @NotBlank
    private String oauthVerifier;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
