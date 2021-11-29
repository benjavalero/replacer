package es.bvalero.replacer.page.review;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Value;
import org.springframework.lang.NonNull;

@ApiModel(description = "Page and replacements to be reviewed")
@Value(staticConstructor = "of")
public class PageReviewDto {

    // TODO: Public while refactoring

    @ApiModelProperty(required = true)
    @NonNull
    PageDto page;

    @ApiModelProperty(value = "List of replacements to review", required = true)
    @NonNull
    List<PageReplacement> replacements;

    @ApiModelProperty(value = "Search options of the replacements to review", required = true)
    @NonNull
    PageReviewSearch search;
}
