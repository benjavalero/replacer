package es.bvalero.replacer.common.dto;

import es.bvalero.replacer.common.domain.AccessToken;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccessTokenDto {

    @Schema(required = true, example = "f8e520e8669a2d65e094d649a96427ff")
    @NotNull
    private String token;

    @Schema(required = true, example = "36dd90e87c59acc138ee0c38487e975af6da141e")
    @NotNull
    private String tokenSecret;

    public static AccessTokenDto fromDomain(AccessToken accessToken) {
        AccessTokenDto dto = new AccessTokenDto();
        dto.setToken(accessToken.getToken());
        dto.setTokenSecret(accessToken.getTokenSecret());
        return dto;
    }

    public static AccessToken toDomain(AccessTokenDto dto) {
        return AccessToken.of(dto.getToken(), dto.getTokenSecret());
    }
}
