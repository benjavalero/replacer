package es.bvalero.replacer.authentication;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.springframework.lang.NonNull;

@ApiModel(description = "Request token, along with the authorization URL, to start authentication.")
@Value(staticConstructor = "of")
class RequestTokenResponse {

    @ApiModelProperty(required = true, example = "b3cecd4b16ecde45d9fd1a0ce68a4091")
    @NonNull
    String token;

    @ApiModelProperty(required = true, example = "23391110732a791d94321559c784c85c")
    @NonNull
    String tokenSecret;

    @ApiModelProperty(
        required = true,
        example = "https://meta.wikimedia.org/wiki/Special:OAuth/authorize?oauth_token=b3cecd4b16ecde45d9fd1a0ce68a4091"
    )
    @NonNull
    String authorizationUrl;
}
