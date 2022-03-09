package es.bvalero.replacer.authentication.dto;

import es.bvalero.replacer.authentication.oauth.RequestToken;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotBlank;

@Schema(description = "Request token to initiate an authorization process")
@Data(staticConstructor = "of")
@NoArgsConstructor
public class RequestTokenDto {

    @Schema(required = true, example = "b3cecd4b16ecde45d9fd1a0ce68a4091")
    @NonNull
    @NotBlank
    String token;

    @Schema(required = true, example = "23391110732a791d94321559c784c85c")
    @NonNull
    @NotBlank
    String tokenSecret;

    public static RequestTokenDto fromDomain(RequestToken requestToken) {
        RequestTokenDto dto = new RequestTokenDto();
        dto.setToken(requestToken.getToken());
        dto.setTokenSecret(requestToken.getTokenSecret());
        return dto;
    }

    public static RequestToken toDomain(RequestTokenDto dto) {
        return RequestToken.of(dto.getToken(), dto.getTokenSecret());
    }
}
