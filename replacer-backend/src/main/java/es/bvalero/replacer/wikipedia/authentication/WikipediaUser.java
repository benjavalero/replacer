package es.bvalero.replacer.wikipedia.authentication;

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

    @ApiModelProperty(required = true, example = "f8e520e8669a2d65e094d649a96427ff")
    String token;

    @ApiModelProperty(required = true, example = "36dd90e87c59acc138ee0c38487e975af6da141e")
    String tokenSecret;
}
