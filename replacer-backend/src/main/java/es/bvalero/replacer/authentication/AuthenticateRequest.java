package es.bvalero.replacer.authentication;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
class AuthenticateRequest {

    @ApiModelProperty(required = true, example = "4360336e38cd5dc8e3f90ad797154275")
    private String requestToken;

    @ApiModelProperty(required = true, example = "533f4ef5d94d89649626123db3dbec35")
    private String requestTokenSecret;

    @ApiModelProperty(required = true, example = "04fd9900f948d22e65c5618b2093cfc5")
    private String oauthVerifier;
}
