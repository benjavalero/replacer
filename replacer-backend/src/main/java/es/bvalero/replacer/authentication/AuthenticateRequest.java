package es.bvalero.replacer.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request token and verifier to complete OAuth authentication")
@Data
@NoArgsConstructor
class AuthenticateRequest {

    @Schema(required = true, example = "4360336e38cd5dc8e3f90ad797154275")
    @NotBlank
    private String token;

    @Schema(required = true, example = "533f4ef5d94d89649626123db3dbec35")
    @NotBlank
    private String tokenSecret;

    @Schema(required = true, example = "04fd9900f948d22e65c5618b2093cfc5")
    @NotBlank
    private String oauthVerifier;
}
