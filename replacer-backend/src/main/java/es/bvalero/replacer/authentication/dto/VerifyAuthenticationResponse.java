package es.bvalero.replacer.authentication.dto;

import es.bvalero.replacer.common.dto.AccessTokenDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Application user with access token after completing the authorization verification")
@Value
@Builder
public class VerifyAuthenticationResponse {

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

    @Schema(description = "Access token authenticating the user to perform operations in Wikipedia", required = true)
    @ToString.Exclude
    @NonNull
    AccessTokenDto accessToken;
}
