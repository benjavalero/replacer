package es.bvalero.replacer.page.review;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Value;

@ApiModel(description = "Page and replacements to be reviewed")
@Value(staticConstructor = "of")
public class PageReviewDto {

    // TODO: Public while refactoring

    @ApiModelProperty(required = true)
    PageDto page;

    @ApiModelProperty(value = "List of replacements to review", required = true)
    List<PageReplacement> replacements;

    @ApiModelProperty(value = "Search options of the replacements to review", required = true)
    PageReviewSearch search;
}
