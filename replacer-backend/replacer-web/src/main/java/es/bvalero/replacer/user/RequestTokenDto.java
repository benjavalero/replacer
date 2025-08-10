package es.bvalero.replacer.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import es.bvalero.replacer.auth.RequestToken;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Schema(
    description = "Request token with an authorization URL to initiate an authorization process",
    name = "RequestToken"
)
@Data
@NoArgsConstructor
class RequestTokenDto {

    @Schema(requiredMode = REQUIRED, example = "b3cecd4b16ecde45d9fd1a0ce68a4091")
    @NonNull
    @NotNull
    String token;

    @Schema(requiredMode = REQUIRED, example = "23391110732a791d94321559c784c85c")
    @NonNull
    @NotNull
    String tokenSecret;

    @Schema(
        requiredMode = REQUIRED,
        example = "https://meta.wikimedia.org/wiki/Special:OAuth/authorize?oauth_token=b3cecd4b16ecde45d9fd1a0ce68a4091"
    )
    @NonNull
    @NotNull
    String authorizationUrl;

    static RequestTokenDto of(RequestToken requestToken) {
        RequestTokenDto dto = new RequestTokenDto();
        dto.setToken(requestToken.getToken());
        dto.setTokenSecret(requestToken.getTokenSecret());
        dto.setAuthorizationUrl(requestToken.getAuthorizationUrl());
        return dto;
    }

    static RequestToken toDomain(RequestTokenDto dto) {
        return RequestToken.of(dto.getToken(), dto.getTokenSecret(), dto.getAuthorizationUrl());
    }
}
