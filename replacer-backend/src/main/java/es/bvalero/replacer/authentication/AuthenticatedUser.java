package es.bvalero.replacer.authentication;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;

@ApiModel(description = "Authenticated user")
@Value
@Builder
class AuthenticatedUser {

    @ApiParam(value = "Name of the user in Wikipedia", required = true, example = "Benjavalero")
    @NonNull
    String name;

    @ApiModelProperty(value = "If the user the rights to use the tool", required = true, example = "true")
    @NonNull
    Boolean hasRights;

    @ApiModelProperty(value = "If the user is a bot", required = true, example = "true")
    @NonNull
    Boolean bot;

    @ApiModelProperty(value = "If the user is administrator of Replacer", required = true, example = "false")
    @NonNull
    Boolean admin;

    @ApiModelProperty(required = true, example = "f8e520e8669a2d65e094d649a96427ff")
    @ToString.Exclude
    @NonNull
    String token;

    @ApiModelProperty(required = true, example = "36dd90e87c59acc138ee0c38487e975af6da141e")
    @ToString.Exclude
    @NonNull
    String tokenSecret;
}
