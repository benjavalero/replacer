package es.bvalero.replacer.authentication.requesttoken;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema
@Value(staticConstructor = "of")
public class GetRequestTokenResponse {

    @Schema(required = true, example = "b3cecd4b16ecde45d9fd1a0ce68a4091")
    @NonNull
    String token;

    @Schema(required = true, example = "23391110732a791d94321559c784c85c")
    @NonNull
    String tokenSecret;

    @Schema(
        required = true,
        example = "https://meta.wikimedia.org/wiki/Special:OAuth/authorize?oauth_token=b3cecd4b16ecde45d9fd1a0ce68a4091"
    )
    @NonNull
    String authorizationUrl;
}
