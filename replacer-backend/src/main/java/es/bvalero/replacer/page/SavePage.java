package es.bvalero.replacer.page;

import es.bvalero.replacer.wikipedia.AccessToken;
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

    @ApiModelProperty(value = "OAuth access token", required = true)
    private AccessToken accessToken;

    @Override
    public String toString() {
        return ("SavePage(page=" + this.getPage() + ", search=" + this.getSearch() + ")");
    }
}
