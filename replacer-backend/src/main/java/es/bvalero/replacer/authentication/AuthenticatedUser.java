package es.bvalero.replacer.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Authenticated user with access token")
@Value
@Builder
class AuthenticatedUser {

    @Schema(description = "Name of the user in Wikipedia", required = true, example = "Benjavalero")
    @NonNull
    String name;

    @Schema(description = "If the user the rights to use the tool", required = true, example = "true")
    @NonNull
    Boolean hasRights;

    @Schema(description = "If the user is a bot", required = true, example = "true")
    @NonNull
    Boolean bot;

    @Schema(description = "If the user is administrator of Replacer", required = true, example = "false")
    @NonNull
    Boolean admin;

    @Schema(required = true, example = "f8e520e8669a2d65e094d649a96427ff")
    @ToString.Exclude
    @NonNull
    String token;

    @Schema(required = true, example = "36dd90e87c59acc138ee0c38487e975af6da141e")
    @ToString.Exclude
    @NonNull
    String tokenSecret;
}
