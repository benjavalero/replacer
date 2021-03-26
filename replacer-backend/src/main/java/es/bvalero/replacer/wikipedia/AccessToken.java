package es.bvalero.replacer.wikipedia;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@ApiModel(description = "OAuth access token")
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessToken {

    @ApiModelProperty(required = true, example = "f8e520e8669a2d65e094d649a96427ff")
    private String token;

    @ApiModelProperty(required = true, example = "36dd90e87c59acc138ee0c38487e975af6da141e")
    private String tokenSecret;

    public static AccessToken of(String token, String tokenSecret) {
        return new AccessToken(token, tokenSecret);
    }

    static AccessToken ofEmpty() {
        return AccessToken.of("", "");
    }
}
