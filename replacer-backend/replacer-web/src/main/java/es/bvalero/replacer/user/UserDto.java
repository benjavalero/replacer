package es.bvalero.replacer.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(
    description = "Application user with access token after completing the authorization verification",
    name = "User"
)
@Value
@Builder(access = AccessLevel.PRIVATE)
class UserDto {

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

    static UserDto of(User user) {
        return UserDto
            .builder()
            .name(user.getId().getUsername())
            .hasRights(user.hasRights())
            .bot(user.isBot())
            .admin(user.isAdmin())
            .build();
    }
}
