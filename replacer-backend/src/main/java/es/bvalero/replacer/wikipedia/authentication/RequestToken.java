package es.bvalero.replacer.wikipedia.authentication;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

@ApiModel(description = "OAuth request token")
@Value(staticConstructor = "of")
class RequestToken {

    @ApiModelProperty(required = true, example = "b3cecd4b16ecde45d9fd1a0ce68a4091")
    String token;

    @ApiModelProperty(required = true, example = "23391110732a791d94321559c784c85c")
    String tokenSecret;

    @ApiModelProperty(
        required = true,
        example = "https://meta.wikimedia.org/wiki/Special:OAuth/authorize?oauth_token=b3cecd4b16ecde45d9fd1a0ce68a4091"
    )
    String authorizationUrl;
}
