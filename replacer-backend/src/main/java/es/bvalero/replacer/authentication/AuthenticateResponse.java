package es.bvalero.replacer.authentication;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;
import lombok.Value;

@ApiModel(description = "Response DTO containing the access token and the user details after authentication")
@Value
class AuthenticateResponse {

    @ApiModelProperty(value = "Wikipedia user name", required = true, example = "Benjavalero")
    String name;

    @ApiModelProperty(value = "If the user the rights to use the tool", required = true, example = "true")
    boolean hasRights;

    @ApiModelProperty(value = "If the user is a bot", required = true, example = "true")
    boolean bot;

    @ApiModelProperty(value = "If the user is administrator of Replacer", required = true, example = "false")
    boolean admin;

    @ApiModelProperty(required = true, example = "f8e520e8669a2d65e094d649a96427ff")
    @ToString.Exclude
    String token;

    @ApiModelProperty(required = true, example = "36dd90e87c59acc138ee0c38487e975af6da141e")
    @ToString.Exclude
    String tokenSecret;

    static AuthenticateResponse of(AccessToken accessToken, WikipediaUser wikipediaUser) {
        return new AuthenticateResponse(
            wikipediaUser.getName(),
            wikipediaUser.hasRights(),
            wikipediaUser.isBot(),
            wikipediaUser.isAdmin(),
            accessToken.getToken(),
            accessToken.getTokenSecret()
        );
    }
}
