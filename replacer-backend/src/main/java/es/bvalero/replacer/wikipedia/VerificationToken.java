package es.bvalero.replacer.wikipedia;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.TestOnly;

@ApiModel(description = "OAuth verification token")
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class VerificationToken {

    @ApiModelProperty(required = true, example = "4360336e38cd5dc8e3f90ad797154275")
    private String requestToken;

    @ApiModelProperty(required = true, example = "533f4ef5d94d89649626123db3dbec35")
    private String requestTokenSecret;

    @ApiModelProperty(required = true, example = "04fd9900f948d22e65c5618b2093cfc5")
    private String oauthVerifier;

    @TestOnly
    static VerificationToken of(String requestToken, String requestTokenSecret, String oauthVerifier) {
        return new VerificationToken(requestToken, requestTokenSecret, oauthVerifier);
    }
}
