package es.bvalero.replacer.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Application user with access token after completing the authorization verification")
@Value
@Builder(access = AccessLevel.PRIVATE)
class VerifyAuthenticationResponse {

    @Schema(description = "Name of the user in Wikipedia", requiredMode = REQUIRED, example = "Benjavalero")
    @NonNull
    String name;

    @Schema(description = "If the user the rights to use the tool", requiredMode = REQUIRED, example = "true")
    @NonNull
    Boolean hasRights;

    @Schema(description = "If the user is a bot", requiredMode = REQUIRED, example = "true")
    @NonNull
    Boolean bot;

    @Schema(description = "If the user is administrator of Replacer", requiredMode = REQUIRED, example = "false")
    @NonNull
    Boolean admin;

    @Schema(
        description = "Access token authenticating the user to perform operations in Wikipedia",
        requiredMode = REQUIRED
    )
    @ToString.Exclude
    @NonNull
    AccessTokenDto accessToken;

    static VerifyAuthenticationResponse of(User user, AccessToken accessToken) {
        return VerifyAuthenticationResponse
            .builder()
            .name(user.getName())
            .hasRights(user.hasRights())
            .bot(user.isBot())
            .admin(user.isAdmin())
            .accessToken(AccessTokenDto.of(accessToken))
            .build();
    }
}
