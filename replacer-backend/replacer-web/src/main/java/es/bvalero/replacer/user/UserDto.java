package es.bvalero.replacer.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Authenticated application user", name = "User")
@Value
@Builder(access = AccessLevel.PRIVATE)
class UserDto {

    @Schema(description = "Name of the user in Wikipedia", requiredMode = REQUIRED, example = "Benjavalero")
    @NonNull
    String name;

    @Schema(description = "If the user the rights to use the tool", requiredMode = REQUIRED, example = "true")
    boolean hasRights;

    @Schema(description = "If the user is a special user, e.g. a patroller", requiredMode = REQUIRED, example = "true")
    boolean specialUser;

    @Schema(description = "If the user is a bot", requiredMode = REQUIRED, example = "true")
    boolean bot;

    @Schema(description = "If the user is administrator of Replacer", requiredMode = REQUIRED, example = "false")
    boolean admin;

    static UserDto of(User user) {
        return UserDto.builder()
            .name(user.getId().getUsername())
            .hasRights(user.hasRights())
            .bot(user.isBot())
            .specialUser(user.isSpecialUser())
            .admin(user.isAdmin())
            .build();
    }

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
