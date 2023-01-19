package es.bvalero.replacer.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Schema(description = "Request token to initiate an authorization process", name = "RequestToken")
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

    static RequestTokenDto of(RequestToken requestToken) {
        RequestTokenDto dto = new RequestTokenDto();
        dto.setToken(requestToken.getToken());
        dto.setTokenSecret(requestToken.getTokenSecret());
        return dto;
    }

    static RequestToken toDomain(RequestTokenDto dto) {
        return RequestToken.of(dto.getToken(), dto.getTokenSecret());
    }
}
