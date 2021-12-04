package es.bvalero.replacer.page.save;

import es.bvalero.replacer.page.review.PageReviewSearch;
import es.bvalero.replacer.page.review.ReviewPage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "Page to update and mark as reviewed. Empty contents is equivalent to review with no changes.")
@Getter
@Setter
class PageSaveRequest {

    @ApiModelProperty(value = "Page to review", required = true)
    @Valid
    private ReviewPage page;

    @ApiModelProperty(value = "Search options of the replacements to review", required = true)
    @Valid
    private PageReviewSearch search;

    @ApiModelProperty(required = true, example = "f8e520e8669a2d65e094d649a96427ff")
    @ToString.Exclude
    @NotNull
    private String token;

    @ApiModelProperty(required = true, example = "36dd90e87c59acc138ee0c38487e975af6da141e")
    @ToString.Exclude
    @NotNull
    private String tokenSecret;
}
