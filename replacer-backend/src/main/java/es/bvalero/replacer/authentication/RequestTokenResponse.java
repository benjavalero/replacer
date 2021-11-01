package es.bvalero.replacer.authentication;

import es.bvalero.replacer.wikipedia.OAuthToken;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ApiModel(
    description = "DTO containing a generated request token, along with the authorization URL, to start authentication."
)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class RequestTokenResponse {

    @ApiModelProperty(required = true, example = "b3cecd4b16ecde45d9fd1a0ce68a4091")
    String token;

    @ApiModelProperty(required = true, example = "23391110732a791d94321559c784c85c")
    String tokenSecret;

    @ApiModelProperty(
        required = true,
        example = "https://meta.wikimedia.org/wiki/Special:OAuth/authorize?oauth_token=b3cecd4b16ecde45d9fd1a0ce68a4091"
    )
    String authorizationUrl;

    static RequestTokenResponse of(OAuthToken requestToken, String authorizationUrl) {
        return new RequestTokenResponse(requestToken.getToken(), requestToken.getTokenSecret(), authorizationUrl);
    }
}
