package es.bvalero.replacer.page;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "Page to update and mark as reviewed. Empty contents is equivalent to review with no changes.")
@Getter
@Setter
class SavePage {

    @ApiModelProperty(value = "Page to review", required = true)
    private PageDto page;

    @ApiModelProperty(value = "Search options of the replacements to review", required = true)
    private PageReviewSearch search;

    @ApiModelProperty(required = true, example = "f8e520e8669a2d65e094d649a96427ff")
    private String token;

    @ApiModelProperty(required = true, example = "36dd90e87c59acc138ee0c38487e975af6da141e")
    private String tokenSecret;

    @Override
    public String toString() {
        return ("SavePage(page=" + this.getPage() + ", search=" + this.getSearch() + ")");
    }
}
