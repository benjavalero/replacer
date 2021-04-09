package es.bvalero.replacer.wikipedia;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

@ApiModel(description = "User authenticated in MediaWiki")
@Value(staticConstructor = "of")
class WikipediaUser {

    @ApiModelProperty(value = "Wikipedia user name", required = true, example = "Benjavalero")
    String name;

    @ApiModelProperty(value = "If the user the rights to use the tool", required = true, example = "true")
    boolean hasRights;

    @ApiModelProperty(value = "If the user is administrator of Replacer", required = true, example = "false")
    boolean admin;

    AccessToken accessToken;
}
