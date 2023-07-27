package es.bvalero.replacer.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Request token and authorization URL to initiate an authorization process")
@Value(staticConstructor = "of")
public class InitiateAuthenticationResponse {

    @Schema
    @NonNull
    RequestTokenDto requestToken;

    @Schema(
        requiredMode = REQUIRED,
        example = "https://meta.wikimedia.org/wiki/Special:OAuth/authorize?oauth_token=b3cecd4b16ecde45d9fd1a0ce68a4091"
    )
    @NonNull
    String authorizationUrl;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
